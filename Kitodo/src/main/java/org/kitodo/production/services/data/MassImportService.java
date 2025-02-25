/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.services.data;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.primefaces.model.file.UploadedFile;

public class MassImportService {

    private static MassImportService instance = null;
    private final List<Character> csvSeparatorCharacters = Arrays.asList(',', ';');

    /**
     * Return singleton variable of type MassImportService.
     *
     * @return unique instance of MassImportService
     */
    public static MassImportService getInstance() {
        MassImportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MassImportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MassImportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Read and return lines from UploadedFile 'file'.
     * @param file UploadedFile for mass import
     * @return list of lines from UploadedFile 'file'
     * @throws IOException thrown if InputStream cannot be read from provided UploadedFile 'file'.
     */
    public List<String> getLines(UploadedFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    /**
     * Split provided lines by given 'separator'-String and return list of CsvRecord.
     * The method also handles quoted csv values, which contain comma or semicolon to allow
     * csv separators in csv cells.
     * @param lines lines to parse
     * @param separator String used to split lines into individual parts
     * @return list of CsvRecord
     */
    public List<CsvRecord> parseLines(List<String> lines, String separator) throws IOException, CsvException {
        List<CsvRecord> records = new LinkedList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator.charAt(0))
                .withQuoteChar('\"')
                .build();
        try (StringReader reader = new StringReader(String.join("\n", lines));
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withSkipLines(0)
                     .withCSVParser(parser)
                     .build()) {
            for (String[] entries : csvReader.readAll()) {
                if (isSingleEmptyEntry(entries)) {
                    continue; // Skip processing this line
                }
                List<CsvCell> cells = new LinkedList<>();
                for (String value : entries) {
                    cells.add(new CsvCell(value.trim()));
                }
                records.add(new CsvRecord(cells));
            }
        }
        return records;
    }

    // Helper method to check if a line has a single empty entry
    private boolean isSingleEmptyEntry(String[] entries) {
        return entries.length == 1 && entries[0].isEmpty();
    }

    /**
     * Import records for given rows, containing IDs for individual download and optionally additional metadata
     * to be added to each record.
     * @param metadataKeys metadata keys for additional metadata added to individual records during import
     * @param records list of CSV records
     */
    public LinkedList<LinkedHashMap<String, List<String>>> prepareMetadata(List<String> metadataKeys, List<CsvRecord> records)
            throws ImportException {
        LinkedList<LinkedHashMap<String, List<String>>> presetMetadata = new LinkedList<>();
        for (CsvRecord record : records) {
            LinkedHashMap<String, List<String>> processMetadata = new LinkedHashMap<>();
            for (int index = 0; index < metadataKeys.size(); index++) {
                String metadataKey = metadataKeys.get(index);
                if (StringUtils.isNotBlank(metadataKey)) {
                    List<String> values = processMetadata.computeIfAbsent(metadataKey, k -> new ArrayList<>());
                    values.add(record.getCsvCells().get(index).getValue());
                }
            }
            presetMetadata.add(processMetadata);
        }
        return presetMetadata;
    }

    /**
     * Get all allowed metadata.
     * @param divisions list of StructuralElementViewInterface
     * @param enteredMetadata collection of preset metadata
     * @return list of allowed metadata as List of ProcessDetail
     */
    public List<ProcessDetail> getAddableMetadataTable(List<StructuralElementViewInterface> divisions,
                                                       Collection<Metadata> enteredMetadata) {
        ProcessFieldedMetadata table = new ProcessFieldedMetadata();
        List<MetadataViewInterface> commonMetadata = new ArrayList<>();
        for (int i = 0; i < divisions.size(); i++) {
            List<MetadataViewInterface> metadataView =
                    divisions.get(i).getAddableMetadata(enteredMetadata, Collections.emptyList())
                            .stream().sorted(Comparator.comparing(MetadataViewInterface::getLabel))
                            .collect(Collectors.toList());
            if (i == 0) {
                commonMetadata = new ArrayList<>(List.copyOf(metadataView));
            } else {
                commonMetadata.removeIf(item -> metadataView.stream()
                        .noneMatch(metadataElement -> Objects.equals(item.getId(), metadataElement.getId())));
            }
            if (commonMetadata.isEmpty()) {
                break;
            }
        }
        for (MetadataViewInterface keyView : commonMetadata) {
            if (!keyView.isComplex()) {
                table.createMetadataEntryEdit((SimpleMetadataViewInterface) keyView, Collections.emptyList());
            }
        }
        return table.getRows();
    }

    /**
     * This method takes a list of lines from a CSV file and tries to guess separator character used in this file
     * (e.g. comma or semicolon). To achieve this, the method gathers the number of occurrences of each candidate in all
     * lines and selects the character with the highest number of lines containing one specific count of that character.
     * character
     * @param csvLines lines from CSV file
     * @return character that is determined to be most likely used as separator character in uploaded CSV file
     */
    // TODO: write test(s)
    public String guessCsvSeparator(List<String> csvLines) {
        Map<Character, Map<Integer, Integer>> separatorOccurrences = new HashMap<>();
        for (char separator : csvSeparatorCharacters) {
            Map<Integer, Integer> currentOccurrences = new HashMap<>();
            for (String line : csvLines) {
                int occurrences = StringUtils.countMatches(line, separator);
                if (currentOccurrences.containsKey(occurrences)) {
                    currentOccurrences.put(occurrences, currentOccurrences.get(occurrences) + 1);
                } else {
                    currentOccurrences.put(occurrences, 1);
                }
            }
            separatorOccurrences.put(separator, currentOccurrences);
        }
        Character probablyCharacter = csvSeparatorCharacters.get(0);
        int maxOccurrence = 0;
        for (Map.Entry<Character, Map<Integer, Integer>> characterStatistics : separatorOccurrences.entrySet()) {
            Optional<Map.Entry<Integer, Integer>> highestOccurrence = characterStatistics.getValue().entrySet()
                    .stream().max(Map.Entry.comparingByValue());
            if (highestOccurrence.isPresent()) {
                Map.Entry<Integer, Integer> occurrence = highestOccurrence.get();
                // skip count of lines that did not contain current separator, e.g. occurrences are "0", which would
                // otherwise be the most common count in the statistic for each unused separator character!
                if (occurrence.getKey() > 0 && occurrence.getValue() > maxOccurrence) {
                    probablyCharacter = characterStatistics.getKey();
                }
            }
        }
        return String.valueOf(probablyCharacter);
    }

    public List<Character> getCsvSeparatorCharacters() {
        return csvSeparatorCharacters;
    }

}
