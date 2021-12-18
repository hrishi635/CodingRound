package jsonproject.jsonproject;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.util.*;

public class CookingMachine {
	
	static int slotsVacant = 7;
	static final int totalSlotsPresent = 7;
	static final double MAX_TIME_TO_COOK = 150.0;
	static final double timeToCookAppetizer = 17.0;
	static final double timeToCookMainCourse = 29.0;
	static final double timeToTravel1KM = 8.0;
	private static final String JSONObject = null;
	
	static HashMap<Integer,Boolean> uniqueOrders = new HashMap<>();
	
	
	public static void main(String[] args) throws Exception {
		List<Orders> orders=new ArrayList<>();
		orders = readInputFromJSONFile(orders);
		List<String> OrderDeliverStatus = calculateEstimatedTimeToDeliver(orders);
		for(String each_order_status: OrderDeliverStatus)
			System.out.println(each_order_status);
	}
	
	static List<Orders> readInputFromJSONFile(List<Orders> orders) throws Exception, ParseException
	{
		JSONParser jsonparser = new JSONParser();
		FileReader reader=new FileReader(".\\jsonfiles\\input.json");
		JSONArray obj = (JSONArray)jsonparser.parse(reader);
		for(Object o : obj)
		{
			JSONObject order = (JSONObject)o;
			String id = (String)order.get("orderId");
			int order_id = Integer.parseInt(id);
			
			String dist = (String)order.get("distance");
			double distance = Double.parseDouble(dist);
			
			JSONArray meals = (JSONArray)order.get("meals");
			String meal[] = new String[meals.size()];
			int index=0;
			for(Object each_meal : meals)
				meal[index++]=(String)each_meal;
			orders.add(new Orders(order_id,meal,distance));		
		}
		return orders;
	}
	
	static List<String> calculateEstimatedTimeToDeliver(List<Orders> orders)
	{
		List<String> OrderStatusList = new ArrayList<>();
		PriorityQueue<SlotsOccupied> CookingOrdersAtPresent = new PriorityQueue<>(new Heap());
		for(Orders eachOrder : orders)
		{
			if(isUniqueOrder(eachOrder))
			{
				int slotsRequired = calculateSlotsRequiredToCook(eachOrder.meals);
				double timeRequiredToDeliver = calculateTimeRequiredToDeliver(eachOrder.meals, eachOrder.distance, slotsRequired);
				if(isOrderPossible(slotsRequired))
					if(timeRequiredToDeliver <= MAX_TIME_TO_COOK)
						createSuccessOrder(OrderStatusList, eachOrder.order_id, timeRequiredToDeliver, slotsRequired, CookingOrdersAtPresent);
					else
						createFailedOrderDueToExcessTime(OrderStatusList, eachOrder.order_id);
				else
					createFailedOrderDueToExcessSlots(OrderStatusList, eachOrder.order_id);
			}
			else
				createFailedOrderDueToOrderID(OrderStatusList, eachOrder.order_id);
		}
		return OrderStatusList;
	}
	
	
	
	static boolean isUniqueOrder(Orders orderItem)
	{
		if(uniqueOrders.containsKey(orderItem.order_id))
			return false;
		uniqueOrders.put(orderItem.order_id, true);
		int length = countLength(orderItem.order_id);
		if(length!=2)
			return false;
		return true;
	}
	
	static int countLength(int value)
	{
		int countDigits=0;
		while(value!=0)
		{
			value/=10;
			countDigits++;
		}
		return countDigits;
	}
	
	
	static int calculateSlotsRequiredToCook(String meals[])
	{
		int countSlotsToCook = 0;
		for(String each_meal: meals)
		{
			if(each_meal.equals("A"))
				countSlotsToCook += 1;
			if(each_meal.equals("M"))
				countSlotsToCook += 2;
		}
		return countSlotsToCook;
	}
	
	static double calculateTimeRequiredToDeliver(String meals[],double distance,int slotsRequired)
	{
		double totalTimeRequiredToDeliver=0.0;
		double maxTimeToCook=0;
		for(String each_meal: meals)
		{
			if(each_meal.equals("A"))
				maxTimeToCook=(double)Math.max(maxTimeToCook, timeToCookAppetizer);
			else
			{
				maxTimeToCook=(double)Math.max(maxTimeToCook, timeToCookMainCourse);
				break;
			}
		}
		totalTimeRequiredToDeliver = maxTimeToCook + (distance*timeToTravel1KM);
		return totalTimeRequiredToDeliver;
	}
	
	static boolean isOrderPossible(int slotsRequired)
	{
		if(slotsRequired<=totalSlotsPresent)
			return true;
		return false;
	}
	
	static void createSuccessOrder(List<String> OrderStatusList, int order_id, double timeRequiredToDeliver, int slotsRequired, PriorityQueue<SlotsOccupied> CookingOrdersAtPresent)
	{
		if(slotsRequired <= slotsVacant)
		{
			slotsVacant -= slotsRequired;
			String SUCCESS_DELIVER_MSG = "Order "+order_id+" will get delivered in "+timeRequiredToDeliver+" minutes";
			OrderStatusList.add(SUCCESS_DELIVER_MSG);
			CookingOrdersAtPresent.add(new SlotsOccupied(slotsRequired, timeRequiredToDeliver));
		}
		else
		{	
			while(!CookingOrdersAtPresent.isEmpty() && (slotsRequired > slotsVacant))
			{
				SlotsOccupied slotsOccupied = CookingOrdersAtPresent.poll();
				timeRequiredToDeliver += slotsOccupied.cookingTime;
				slotsVacant += slotsOccupied.slots;		
			}
			if(slotsRequired <= slotsVacant)
			{
				slotsVacant -= slotsRequired;
				String SUCCESS_DELIVER_MSG = "Order "+order_id+" will get delivered in "+timeRequiredToDeliver+" minutes";
				OrderStatusList.add(SUCCESS_DELIVER_MSG);
				CookingOrdersAtPresent.add(new SlotsOccupied(slotsRequired, timeRequiredToDeliver));
			}
			else
			{
				System.out.println("Handle case in createSuccessOrder()");
			}
		}
	}
	
	static void createFailedOrderDueToExcessTime(List<String> OrderStatusList,int order_id)
	{
		String EXCESS_TIME_ORDER_FAILURE = "Order "+order_id+" is denied because the restaurant cannot accommodate it.";
		OrderStatusList.add(EXCESS_TIME_ORDER_FAILURE);
	}
	
	static void createFailedOrderDueToExcessSlots(List<String> OrderStatusList, int order_id)
	{
		String EXCESS_SLOTS_ORDER_FAILURE = "Order "+order_id+" is denied because the restaurant cannot accommodate it.";
		OrderStatusList.add(EXCESS_SLOTS_ORDER_FAILURE);
	}
	
	static void createFailedOrderDueToOrderID(List<String> OrderStatusList, int order_id)
	{
		String NOT_UNIQUE_ORDER_ID = "Order "+order_id+" is denied because (Order ID is not unique)OR(order_id is not a 2 digit number).";
		OrderStatusList.add(NOT_UNIQUE_ORDER_ID);
	}
	
	
	static class Orders{
		int order_id;
		String[] meals;
		double distance;	
		public Orders(int order_id,String[] meals,double distance)
		{
			this.order_id=order_id;
			this.meals=meals;
			this.distance=distance;
		}
	}
	
	public static class SlotsOccupied{
		int slots;
		double cookingTime;
		public SlotsOccupied(int slots,double cookingTime)
		{
			this.slots=slots;
			this.cookingTime=cookingTime;
		}
	}
	
	static class Heap implements Comparator<SlotsOccupied>{
		public int compare(SlotsOccupied a,SlotsOccupied b)
		{
			return (int)a.cookingTime-(int)b.cookingTime; 
		}
	}
}
