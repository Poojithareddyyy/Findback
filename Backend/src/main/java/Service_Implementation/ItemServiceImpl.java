package Service_Implementation;

import java.util.List;

import Data_Layer.ItemDAO;
import Model_Layer.Item;
import Service_Layer.ItemService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class ItemServiceImpl implements ItemService {

    @Override
    public boolean reportItem(int organizationId, int reportedBy,
                              String itemName, String category,
                              String description, String location,
                              String itemDate, String itemType) {

        String status;

        if (itemType.equalsIgnoreCase("LOST")) {
            status = "LOST_OPEN";
        } else if (itemType.equalsIgnoreCase("FOUND")) {
            status = "FOUND_REPORTED";
        } else {
            return false;
        }

        Item item = new Item(
            organizationId,
            reportedBy,
            itemName,
            category,
            description,
            location,
            itemDate,
            itemType.toUpperCase(),
            status
        );

        ItemDAO dao = new ItemDAO();

        return dao.addItem(item);
    }

	@Override
	public List<Item> getItemsByOrganization(int organizationId) {
		ItemDAO dao = new ItemDAO();

	    return dao.getItemsByOrganization(organizationId);
	}
	@Override
	public boolean verifyFoundItem(int itemId, int organizationId, int adminId) {

	    ItemDAO dao = new ItemDAO();

	    return dao.verifyFoundItem(itemId, organizationId, adminId);
	}
	@Override
	public List<Item> getAvailableItems(int organizationId) {

	    ItemDAO dao = new ItemDAO();

	    return dao.getAvailableItems(organizationId);
	}
	
	@Override
	public List<String[]> getMyItems(int organizationId, int userId) {
		
		ItemDAO dao = new ItemDAO();
	    return dao.getMyItems(organizationId, userId);
	}
	
	@Override
	public int getItemCount(int organizationId, String status) {

	    ItemDAO dao = new ItemDAO();

	    return dao.getItemCount(organizationId, status);
	}
	

@Override
public List<Item> getMatchingItems(
        int organizationId,
        String itemName,
        String category,
        String location,
        String itemDate) {

    ItemDAO dao = new ItemDAO();

    // Get verified and available found items
    List<Item> items = dao.getAvailableItems(organizationId);

    String lostName = itemName.toLowerCase().trim();
    String lostCategory = category.toLowerCase().trim();
    String lostLocation = location.toLowerCase().trim();

    LocalDate lostDate = LocalDate.parse(itemDate);

    for (Item item : items) {

        int score = 0;

        // 1. Item name matching: 40 points
        String foundName = item.getItemName().toLowerCase().trim();

        if (foundName.equals(lostName)) {
            score += 40;
        } else if (foundName.contains(lostName)
                || lostName.contains(foundName)) {
            score += 30;
        }

        // 2. Category matching: 30 points
        if (item.getCategory() != null &&
                item.getCategory().equalsIgnoreCase(category)) {
            score += 30;
        }

        // 3. Location matching: 20 points
        String foundLocation = item.getLocation().toLowerCase().trim();

        if (foundLocation.equals(lostLocation)) {
            score += 20;
        } else if (foundLocation.contains(lostLocation)
                || lostLocation.contains(foundLocation)) {
            score += 10;
        }

        // 4. Date proximity: 10 points
        LocalDate foundDate = LocalDate.parse(item.getItemDate());

        long days = Math.abs(
            ChronoUnit.DAYS.between(lostDate, foundDate)
        );

        if (days <= 7) {
            score += 10;
        } else if (days <= 30) {
            score += 5;
        }

        item.setMatchScore(score);
    }

    // Highest matching score first
    items.sort(
        Comparator.comparingInt(Item::getMatchScore).reversed()
    );

    return items;
}

	
	
	
}