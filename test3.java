import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.html.HTMLDocument.Iterator;

public class test3 {

  public static void main(String[] args) {
   
    
      Map<Integer, String> map = new ConcurrentHashMap<Integer, String>();

      map.put(1, "2");
      map.put(2, "2");
      map.put(3, "2");     
      map.put(4, "4");
      map.put(5, "5");
      map.put(6, "6");
      map.put(7, "7");   
      map.put(8, "8");
      map.put(9, "9");

      for (Integer key : map.keySet()) {
          if (key > 4) {
              map.remove(key);
          }
      }

      System.out.println(map);

  }
}