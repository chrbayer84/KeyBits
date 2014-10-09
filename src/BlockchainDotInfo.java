import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class BlockchainDotInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
	}
	
	public static String[] getKeys(String address) throws Exception{
		String[][] outputs = BlockchainDotInfo.getOutputAddressesFromBlockchainDotInfo(address);

		if (outputs.length > 1)
			throw new Exception("this address was revoked by creating more than one outgoing transaction");
		
		return BlockchainDotInfo.arraysToStringArray(BlockchainDotInfo.revert(outputs));
	}
	
	private static String getBlockchainInfo(String url_string) throws Exception{
        URL url = new URL(url_string);  
        URLConnection c = url.openConnection();  
        //Spoof the connection so we look like a web browser  
        c.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)" );  

        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));  

        StringBuffer data = new StringBuffer();
        String line = null;  
        while ((line = br.readLine()) != null)
        	data.append(line + "\n");  
        
        // Close the input stream  
        br.close();
        
        return data.toString();
	}
	
	public static String[][] getInputAddressesFromBlockchainDotInfo(String address) throws Exception{
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		
		String blockchain_info = BlockchainDotInfo.getBlockchainInfo("https://blockchain.info/address/" + address + "?format=json");

		JSONParser parser=new JSONParser();
		JSONObject obj = (JSONObject)parser.parse(blockchain_info);

		JSONArray arr = (JSONArray)obj.get("txs");
		for (int i = 0; i < arr.size(); i++){
			JSONObject tx = (JSONObject)arr.get(i);
			JSONArray inputs = (JSONArray)tx.get("inputs");
			
			ArrayList<String> transaction = new ArrayList<String>();
			for (int j = 0; j < inputs.size(); j++){
				JSONObject input = (JSONObject)inputs.get(j);
				JSONObject prevo = (JSONObject)input.get("prev_out");
				String input_address = (String)prevo.get("addr");
				if (!input_address.equals(address))
					transaction.add(input_address);
			}
			ret.add(transaction);
		}
		
		return BlockchainDotInfo.listsToStringArrays(ret);
	}
	
	public static String[][] getOutputAddressesFromBlockchainDotInfo(String address) throws Exception{
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		
		String blockchain_info = BlockchainDotInfo.getBlockchainInfo("https://blockchain.info/address/" + address + "?format=json");

		JSONParser parser=new JSONParser();
		JSONObject obj = (JSONObject)parser.parse(blockchain_info);

		JSONArray arr = (JSONArray)obj.get("txs");
		for (int i = 0; i < arr.size(); i++){
			JSONObject tx = (JSONObject)arr.get(i);

			ArrayList<String> transaction = new ArrayList<String>();
			if (BlockchainDotInfo.isOutput(address, (JSONArray)tx.get("inputs"))){
				JSONArray outputs = (JSONArray)tx.get("out");
				for (int j = 0; j < outputs.size(); j++){
					JSONObject output = (JSONObject)outputs.get(j);
					String output_address = (String)output.get("addr");
					if (!output_address.equals(address))
						transaction.add(output_address);
				}
			}
			if (transaction.size() > 0)
				ret.add(transaction);
		}
		
		return BlockchainDotInfo.listsToStringArrays(ret);
	}
	
	public static boolean isOutput(String address, JSONArray inputs){
		JSONObject input = (JSONObject)inputs.get(0);
		JSONObject prevo = (JSONObject)input.get("prev_out");
		String input_address = (String)prevo.get("addr");
		
		return input_address.equals(address); 
	}
	
	public static String[] arraysToStringArray(String[][] arrays){
		int size = 0;
		for (int i = 0; i < arrays.length; i++)
			size = size + arrays[i].length;

		String[] ret = new String[size];
		
		int c = 0;
		for (int i = 0; i < arrays.length; i++){
			String[] array = arrays[i];
			for (int j = 0; j < array.length; j++){
				ret[c] = array[j];
				c++;
			}
		}
		
		return ret;
	}
	
	public static String[][] listsToStringArrays(ArrayList<ArrayList<String>> lists){
		String[][] ret = new String[lists.size()][];
		
		for (int i = 0; i < lists.size(); i++){
			ArrayList<String> list = lists.get(i);
			String[] array = new String[list.size()];
			for (int j = 0; j < list.size(); j++)
				array[j] = list.get(j);
			ret[i] = array;
		}
		
		return ret;
	}
	
	public static String[][] revert(String[][] arrays){
		String[][] ret = new String[arrays.length][];
		
		for (int i = arrays.length - 1; i >= 0; i--)
			ret[arrays.length - 1 - i] = arrays[i];
		
		return ret;
	}
}
