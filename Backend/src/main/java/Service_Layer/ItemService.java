package Service_Layer;

import java.util.List;

import Model_Layer.Item;

public interface ItemService {

    boolean reportItem(int organizationId, int reportedBy,
                       String itemName, String category,
                       String description, String location,
                       String itemDate, String itemType);
    
    
    List<Item> getItemsByOrganization(int organizationId);
    boolean verifyFoundItem(int itemId, int organizationId, int adminId);
    List<Item> getAvailableItems(int organizationId);
    
    List<String[]> getMyItems(int organizationId, int userId);
    
    int getItemCount(int organizationId, String status);
    List<Item> getMatchingItems(
    	    int organizationId,
    	    String itemName,
    	    String category,
    	    String location,
    	    String itemDate
    	);
    
    
    
    
}