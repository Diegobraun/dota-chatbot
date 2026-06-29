package com.diegobraun;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("dota")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class DotaResource {

    @Inject
    DotaAssistant dotaAssistant;

    @POST
    public String chat (String userMessage){
        return dotaAssistant.chat(userMessage);
    }
}
