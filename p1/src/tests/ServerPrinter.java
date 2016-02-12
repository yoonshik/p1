package tests;

import cmsc433.p1.AuctionServer;
import cmsc433.p1.Item;

import java.util.List;


/**
 * Wraps around the Server class, providing some logging for each method. 
 * 
 * This class is used for testing purposes.
 *
 */
public class ServerPrinter extends AuctionServer
{
    private static ServerPrinter instance = new ServerPrinter();
    
    private ServerPrinter() { }
    
    public static AuctionServer getInstance() { return instance; }
    
    @Override
    public int soldItemsCount()
    {
        Logger.getInstance().logStart("soldItemsCount");
        
        int returnValue = super.soldItemsCount();
        Logger.getInstance().logEnd("soldItemsCount", returnValue);
        
        return returnValue;
    }

    @Override
    public int revenue()
    {
        Logger.getInstance().logStart("revenue");
        
        int returnValue = super.revenue();
        Logger.getInstance().logEnd("revenue", returnValue);
        
        return returnValue;
    }

    @Override
    public int submitItem(String clientName, String itemName, int lowestBiddingPrice, int biddingDurationSeconds)
    {
        Logger.getInstance().logStart("submitItem", clientName, itemName, lowestBiddingPrice, biddingDurationSeconds);
        
        int returnValue = super.submitItem(clientName, itemName, lowestBiddingPrice, biddingDurationSeconds);
        Logger.getInstance().logEnd("submitItem", returnValue, clientName, itemName, lowestBiddingPrice, biddingDurationSeconds);
        
        return returnValue;
    }

    @Override
    public List<Item> getItems()
    {
        Logger.getInstance().logStart("getItems");
        
        List<Item> returnValue = super.getItems();
        Logger.getInstance().logEnd("getItems", returnValue);
        
        return returnValue;
    }

    @Override
    public boolean submitBid(String clientName, int listingID, int biddingAmount)
    {
        Logger.getInstance().logStart("submitBid", clientName, listingID, biddingAmount);
        
        boolean returnValue = super.submitBid(clientName, listingID, biddingAmount);
        Logger.getInstance().logEnd("submitBid", returnValue, clientName, listingID, biddingAmount);
        
        return returnValue;
    }

    @Override
    public int checkBidStatus(String clientName, int listingID)
    {
        Logger.getInstance().logStart("checkBidStatus", clientName, listingID);
        
        int returnValue = super.checkBidStatus(clientName, listingID);
        Logger.getInstance().logEnd("checkBidStatus", returnValue, clientName, listingID);
        
        return returnValue;
    }

    @Override
    public int itemPrice(int listingID)
    {
        Logger.getInstance().logStart("itemPrice", listingID);
        
        int returnValue = super.itemPrice(listingID);
        Logger.getInstance().logEnd("itemPrice", returnValue, listingID);
        
        return returnValue;
    }
}
