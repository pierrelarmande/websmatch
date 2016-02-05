package org.inria.websmatch.tests;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.inria.websmatch.utils.L;

public class PlantNetDSPLTest {
    
 // Object for Lat/Lon pair
    public class LatLonDecimal
    {
        public float lat = 0.0f;
        public float lon = 0.0f;
        
     // Convert string e.g. "21081686N,079030977E" to Lat/Lon pair
        public LatLonDecimal convert(String latlon)
        {
            String[] parts = latlon.split(",");

            LatLonDecimal position = new LatLonDecimal();
            position.lat = convertPart(parts[0]);
            position.lon = convertPart(parts[1]);
            return position;
        }

        // Convert substring e.g. "21081686N" to decimal angle
        private float convertPart(String angle)
        {
            while (angle.length() < 10)
                angle = new StringBuffer(angle).insert(0, "0").toString();

            int deg = Integer.parseInt( angle.substring(0,2) );
            int min = Integer.parseInt( angle.substring(3,4) );
            int sec = Integer.parseInt( angle.substring(5,6) );
            int sub = Integer.parseInt( angle.substring(7,8) );
            String hem = angle.substring(9);

            float value = deg + min / 60.0f + sec / 3600.0f + sub / 360000.0f;
            float sign = (hem.equals("S")) ? -1.0f : 1.0f; // negative southern hemisphere latitudes
            return sign * value;
        }
    }

    public static void upload() {
	// now upload file using post method
	HttpClient httpclient = new DefaultHttpClient();
	httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

	HttpPost httppost = new HttpPost("http://localhost:8320/api/dspl/load");

	String filePath = "/home/manu/stats.zip";
	File file = new File(filePath);

	// not dsplengine case
	if (!filePath.endsWith(".zip")) {
	    if (file.exists())
		file.delete();

	    try {
		// file
		FileOutputStream fos = new FileOutputStream(file);
		ZipOutputStream zos = new ZipOutputStream(fos);
		byte bytes[] = new byte[2048];

		File dir = new File(filePath.substring(0, filePath.lastIndexOf(".")));

		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
		    FileInputStream fis = new FileInputStream(files[i].getAbsolutePath());
		    BufferedInputStream bis = new BufferedInputStream(fis);

		    zos.putNextEntry(new ZipEntry(files[i].getName()));

		    int bytesRead;
		    while ((bytesRead = bis.read(bytes)) != -1) {
			zos.write(bytes, 0, bytesRead);
		    }
		    zos.closeEntry();
		    bis.close();
		}

		zos.flush();
		fos.flush();
		zos.close();
		fos.close();

	    } catch (IOException e) {
		L.Error(e.getMessage(), e);
	    }
	}

	MultipartEntity mpEntity = new MultipartEntity();
	ContentBody cbFile = new FileBody(file, "multipart/form-data");
	mpEntity.addPart("dspl", cbFile);
	try {
	    mpEntity.addPart("reference", new StringBody("stats"));
	    mpEntity.addPart("directLoading", new StringBody("true"));
	    mpEntity.addPart("overwrite", new StringBody("true"));
	} catch (UnsupportedEncodingException e2) {
	    e2.printStackTrace();
	}

	httppost.setEntity(mpEntity);

	HttpResponse response = null;
	try {
	    response = httpclient.execute(httppost);
	} catch (ClientProtocolException e1) {
	    e1.printStackTrace();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	HttpEntity resEntity = response.getEntity();

	if (resEntity != null) {
	    try {
		System.out.println(EntityUtils.toString(resEntity));
	    } catch (ParseException e) {
		L.Error(e.getMessage(),e);
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}
	if (resEntity != null) {
	    try {
		resEntity.consumeContent();
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}

	httpclient.getConnectionManager().shutdown();

	try {
	    Thread.sleep(500);
	} catch (InterruptedException e) {
	    L.Error(e.getMessage(),e);
	}
    }

    public static void main(String[] args) {

	// upload();
	// System.exit(0);

	HashMap<String, HashMap<String, Integer>> resMap = new HashMap<String, HashMap<String, Integer>>();

	String csvPath = "/home/manu/stats.csv";
	File csvFile = new File(csvPath);

	String zonePath = "/home/manu/stats_zone.csv";
	File zoneFile = new File(zonePath);

	String outPath = "/home/manu/stats_count.csv";
	File outFile = new File(outPath);

	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(csvFile));
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

	try {
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		String[] content = line.split(";");

		// restrict to .1
		/*if (content[1].indexOf('.') != -1) {
		    content[1] = content[1].substring(0, content[1].indexOf('.') + 2);
		}
		if (content[2].indexOf('.') != -1) {
		    content[2] = content[2].substring(0, content[2].indexOf('.') + 2);
		}*/
		//
		
		try {
		    //float f = Float.valueOf(content[1].trim().substring(0,content[1].length()-1)).floatValue();
		    //f = Float.valueOf(content[2].trim().substring(0,content[2].length()-1)).floatValue();

		    content[1] = content[1].trim().substring(0,content[1].length()-1);
		    content[2] = content[2].trim().substring(0,content[2].length()-1);
		    
		    // ok
		    if (resMap.get(content[1]) == null) {
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			temp.put(content[2], 1);
			resMap.put(content[1], temp);
		    } else {
			HashMap<String, Integer> temp = resMap.get(content[1]);
			if (temp.get(content[2]) == null) {
			    temp.put(content[2], 1);
			    resMap.put(content[1], temp);
			} else {
			    Integer val = temp.get(content[2]);
			    val++;
			    temp.put(content[2], val);
			    resMap.put(content[1], temp);
			}
		    }

		} catch (NumberFormatException nfe) {

		}
	    }
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	BufferedWriter writer = null;
	BufferedWriter statWriter = null;

	try {
	    writer = new BufferedWriter(new FileWriter(zoneFile));
	    writer.write("ident,name,latitude,longitude\n");

	    statWriter = new BufferedWriter(new FileWriter(outFile));
	    statWriter.write("year,ident,count\n");

	    int count = 1;
	    Set<String> latSet = resMap.keySet();
	    for (String lat : latSet) {
		HashMap<String, Integer> longMap = resMap.get(lat);
		Set<String> longSet = longMap.keySet();
		for (String longitude : longSet) {
		    writer.write("zone" + count + "," + "zone" + count + "," + lat + "," + longitude + "\n");
		    statWriter.write("2014,zone" + count + "," + longMap.get(longitude) + "\n");
		    count++;
		    if (count > 995) {
			try {
			    writer.flush();
			    writer.close();
			    statWriter.flush();
			    statWriter.close();
			    reader.close();
			    System.exit(0);
			} catch (IOException e) {
			    L.Error(e.getMessage(),e);
			}
		    }
		}
	    }

	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

	try {
	    writer.flush();
	    writer.close();
	    statWriter.flush();
	    statWriter.close();
	    reader.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }

}
