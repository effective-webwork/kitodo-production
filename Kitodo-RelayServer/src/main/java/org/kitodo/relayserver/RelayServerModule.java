package org.kitodo.relayserver;

import org.kitodo.api.relayserver.RelayServerModuleInterface ;

public class RelayServerModule implements RelayServerModuleInterface {

    public String test() {
       return("RELAY SERVER MODULE BACKEND") ;
    }
}
