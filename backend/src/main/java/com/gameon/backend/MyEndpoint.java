/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.gameon.backend;

import com.gameon.backend.controller.SudokuMessageHandler;
import com.gameon.backend.networkingtypes.Packet;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

/**
 * An endpoint class we are exposing
 */
@Api(name = "gameonApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.gameon.com", ownerName = "backend.gameon.com", packagePath = ""))
public class MyEndpoint {

    @ApiMethod(name = "gameonApi.sendMessage",
    httpMethod = ApiMethod.HttpMethod.POST)
    public Packet[] sendMessage(Packet packet) {
        return SudokuMessageHandler.getInstance().handleMessage(packet.payload,packet.date);
    }

//    @ApiMethod(name = "gameonApi.pollMessageQueue",
//    httpMethod = ApiMethod.HttpMethod.POST)
//    public Packet[] pollMessageQueue(@Named("clientId") int clientId){
//        Client client = TemporaryDB.getInstance().findClient(clientId);
//        if(client == null)
//            return new Packet[0];
//        Packet[] packets = MessageQueues.getInstance().getAwaitingPackets(client);
//        return packets;
//    }

}
