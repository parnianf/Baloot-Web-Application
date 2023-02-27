package org.Baloot;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

public class Commodity {
    //@Id
    private String id;
    private String name; //unique validation + handling !@#
    private String providerId;
    private Float price;
    private List<String> categories;
    private HashMap<String, Float> usersRating = new HashMap<>();
    private Float rating;
    private int inStock;
    public Commodity(String id, String name, String providerId, Float price, List<String> categories, Float rating, int inStock) {
        this.id = id;
        this.name = name;
        this.providerId = providerId;
        this.price = price;
        this.categories = categories;
        this.rating = rating;
        this.inStock = inStock;
        this.usersRating.put("###", rating);
    }

    public void addUserRating(String username, Float rating){
        if (usersRating.containsKey(username)){
            usersRating.replace(username, rating);
        }
        else {
            usersRating.put(username, rating);
        }
        updateRating();
    }

    private void updateRating(){
        float sum = 0;
        for (HashMap.Entry<String, Float> entry : usersRating.entrySet()) {
            sum += entry.getValue();
        }
        rating = sum / usersRating.size();
    }

    public boolean isEqual(String id) {
        return this.id.equals(id);
    }

//    public void reduceInStock(int count){
//        if(inStock - count < 0){
//            throw new Exception("Error: Out of Stock.");
//        }
//        else {
//            inStock -= count;
//        }
//    }

}
