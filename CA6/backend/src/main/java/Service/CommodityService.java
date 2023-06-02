package Service;

import Domain.Commodity;
import Domain.Provider;
import Domain.Rating;
import Exceptions.*;
import HTTPRequestHandler.HTTPRequestHandler;
import Repository.CommodityRepository;
import Repository.ProviderRepository;
import Repository.RatingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Service("commodityService")
public class CommodityService {
    private final CommodityRepository commodityRepository;
    private final ProviderRepository providerRepository;
    private final RatingRepository ratingRepository;

    private CommodityService(CommodityRepository commodityRepository, ProviderRepository providerRepository, RatingRepository ratingRepository) throws Exception {
        this.commodityRepository = commodityRepository;
        this.providerRepository = providerRepository;
        this.ratingRepository = ratingRepository;
        fetchData();
    }

    private void fetchData() throws Exception {
        final String COMMODITIES_URI = "http://5.253.25.110:5000/api/v2/commodities";
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Commodity> commodities = objectMapper.readValue(HTTPRequestHandler.getRequest(COMMODITIES_URI), typeFactory.constructCollectionType(List.class, Commodity.class));
        for(Commodity commodity: commodities){
            commodityRepository.save(commodity);
            rateCommodity("#InitialRating", commodity.getId(), commodity.getRating());
        }
        commodityRepository.saveAll(commodities);
    }

    public Commodity findCommodityById(Integer id) throws CommodityNotFound {
        return commodityRepository.findById(id)
                .orElseThrow(CommodityNotFound::new);
    }

    public List<Commodity> getCommodities() {
        return commodityRepository.findAll();
    }

    public List<Commodity> searchCommoditiesByCategory(String category) {
        return commodityRepository.findByCategories(category);
    }

    public List<Commodity> searchCommoditiesByName(String name) {
        return commodityRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Commodity> searchCommoditiesByProviderName(String name) {
        Provider provider = providerRepository.findByName(name);
        return commodityRepository.findByProviderId(provider.getId());
    }

    public List<Commodity> getAvailableCommodities(List<Commodity> commodities) {
        List<Commodity> availableCommodities = new ArrayList<>();
        for (Commodity commodity : commodities) {
            if (commodity.getInStock() > 0) {
                availableCommodities.add(commodity);
            }
        }
        return availableCommodities;
    }

    public List<Commodity> sortCommoditiesByPrice(List<Commodity> commodities) {
        return commodities.stream()
                .sorted(Comparator.comparing(Commodity::getPrice))
                .collect(Collectors.toList());
    }

    public List<Commodity> sortCommoditiesByName(List<Commodity> commodities) {
        return commodities.stream()
                .sorted(Comparator.comparing(Commodity::getName))
                .collect(Collectors.toList());
    }


    public List<Commodity> getCommoditiesByPage(int pageNumber, int itemsPerPage, List<Commodity> allCommodities) {
        int startIndex = pageNumber * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allCommodities.size());
        allCommodities = allCommodities.subList(startIndex, endIndex);
        return allCommodities;
    }

    public List<Commodity> getSuggestedCommodities(Integer commodityId) throws CommodityNotFound {
        Commodity commodity = findCommodityById(commodityId);

        return getCommodities().stream()
                .filter(c -> !c.getId().equals(commodityId)) // exclude the commodity with the same ID
                .sorted(Comparator.comparing(c -> 11 * (commodity.isInSimilarCategory(c.getCategories()) ? 1 : 0) + c.getRating(), Comparator.reverseOrder()))
                .limit(4)
                .collect(Collectors.toList());
    }

    public void rateCommodity(String username, Integer commodityId, Float score) throws RatingOutOfRange, CommodityNotFound { //TODO: solve repeated ratings with same username
        if (score < 1 || score > 10)
            throw new RatingOutOfRange();
        Commodity commodity = findCommodityById(commodityId);
        Rating rating = new Rating(username, commodityId, score); //TODO: check existence of username and commodityId
        ratingRepository.save(rating);
        List<Float> scores = ratingRepository.findScoresByCommodityId(commodityId);
        commodity.updateRatingBasedOnUserRatings(scores);
        commodityRepository.save(commodity);
    }
}