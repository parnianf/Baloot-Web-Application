package org.Baloot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.util.ArrayList;
import java.util.List;

public class Baloot {
    List<User> users = new ArrayList<>();
    List<Commodity> commodities = new ArrayList<>();
    List<Provider> providers = new ArrayList<>();
    ObjectMapper mapper;
    ObjectNode responseNode;

    public Baloot(){
        mapper = new ObjectMapper();
        responseNode = mapper.createObjectNode();
    }

    private User findUserByUsername(String username){
        for (User user : users){
            if (user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }
    private boolean doesUserExist(User newUser) {
        for (User user : users)
            if (user.isEqual(newUser.getUsername()))
                return true;
        return false;
    }

    private boolean doesProviderExist(Provider newProvider) {
        for (Provider provider : providers)
            if (provider.isEqual(newProvider.getId()))
                return true;
        return false;
    }

    public Response addUser(User newUser){
        if (doesUserExist(newUser)) {
            findUserByUsername(newUser.getUsername()).update(newUser);
        }
        else {
            users.add(newUser);
        }
        responseNode.set("Response", mapper.convertValue("User Added.", JsonNode.class));
        return new Response(true, responseNode);
        //TODO: Handling errors of Adding User
    }
     public Response addProvider(Provider newProvider) throws Exception{
         if (!doesProviderExist(newProvider)){
            providers.add(newProvider);
            responseNode.set("Response", mapper.convertValue("Provider Added.", JsonNode.class));
            return new Response(true, responseNode);
         }
         else {
            responseNode.set("Response", mapper.convertValue("Provider Already Exists.", JsonNode.class));
            return new Response(false, responseNode);
         }
     }

     public Provider findProviderById(String providerId){
        for (Provider provider : providers){
            if(provider.isEqual(providerId)){
                return provider;
            }
        }
        return null;
     }

    public Commodity findCommodityById(String commodityId){
        for (Commodity commodity : commodities){
            if(commodity.isEqual(commodityId)){
                return commodity;
            }
        }
        return null;
    }

     public Response addCommodity(Commodity newCommodity) throws Exception{
        if(findProviderById(newCommodity.getProviderId()) != null){
            commodities.add(newCommodity);
            responseNode.set("Response", mapper.convertValue("Commodity Added.", JsonNode.class));
            return new Response(true, responseNode);
        }
        else {
            responseNode.set("Response", mapper.convertValue("Provider NOT Exists.", JsonNode.class));
            return new Response(false, responseNode);
        }
        //TODO: What if commodity already exist?
     }

     public Response getCommoditiesList() throws Exception{
         ObjectMapper objectMapper = new ObjectMapper();
         List<ObjectNode> JsonCommodities = new ArrayList<>();
         for (Commodity entry : commodities) {
             ObjectNode node = objectMapper.convertValue(entry, ObjectNode.class);
             JsonCommodities.add(node);
         }
         ArrayNode arrayNode = objectMapper.valueToTree(JsonCommodities);
         ObjectNode commoditiesList = objectMapper.createObjectNode();
         commoditiesList.putArray("CommoditiesList").addAll(arrayNode);
//         String data = objectMapper.writeValueAsString(commoditiesList);
         return new Response(true, commoditiesList);
     }

     public Response rateCommodity(String username, String commodityId, float score) throws Exception {
         if(findCommodityById(commodityId) == null){
             responseNode.set("Response", mapper.convertValue("Commodity does not exist.", JsonNode.class));
             return new Response(false, responseNode);
         }
         if(findUserByUsername(username) == null){
             responseNode.set("Response", mapper.convertValue("User does not exist.", JsonNode.class));
             return new Response(false, responseNode);
         }
         findCommodityById(commodityId).addUserRating(username, score);
         responseNode.set("Response", mapper.convertValue("Rating Added.", JsonNode.class));
         return new Response(true, responseNode);
     }

    public void printData(){
        System.out.println("Users:");
        for(User user : users){
            System.out.println(user.getUsername());
        }
        System.out.println("Providers:");
        for(Provider provider : providers){
            System.out.println(provider.getId());
        }
        System.out.println("Commodities:");
        for(Commodity commodity : commodities){
            System.out.println(commodity.getId());
        }
    }

}

