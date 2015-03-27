package com.p2p.misc;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HaKr on 27/01/15.
 */
public class LocationHelper {
    public static final String URL_GOOGLE_MMAP = "http://www.google.com/glm/mmap";

    public static Location getLocation(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        if(null != cellLocation){
            int cid = cellLocation.getCid();
            int lac = cellLocation.getLac();

            return getLocation(cid, lac);
        }
        return null;
    }

    private static Location getLocation(int cid, int lac) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(URL_GOOGLE_MMAP);
            URLConnection conn = url.openConnection();
            httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.connect();

            OutputStream outputStream = httpConn.getOutputStream();
            writeData(outputStream, cid, lac);

            InputStream inputStream = httpConn.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            dataInputStream.readShort();
            dataInputStream.readByte();
            int code = dataInputStream.readInt();
            if (code == 0) {
                double lat = (double) dataInputStream.readInt() / 1000000D;
                double lon = (double) dataInputStream.readInt() / 1000000D;
                int i = dataInputStream.readInt();
                int j = dataInputStream.readInt();
                String s = dataInputStream.readUTF();
                dataInputStream.close();

                Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                loc.setLatitude(lat);
                loc.setLongitude(lon);
                //loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
                loc.setTime(System.currentTimeMillis());
                return loc;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (null != httpConn) {
                httpConn.disconnect();
            }
        }

        return null;

    }

    private static void writeData(OutputStream out, int cid, int lac)
            throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        if (cid >= 65536) {
            // Unicom 3G
            dataOutputStream.writeInt(5);
        } else {
            // Mobile 3G
            dataOutputStream.writeInt(3);
        }
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cid);
        dataOutputStream.writeInt(lac);

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }

    private static List<CellInfo> getCellInfo(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = manager.getPhoneType();
        int countryCode;
        int networkCode;
        int areaCode;
        List<CellInfo> cells = new ArrayList<CellInfo>();
        if (type == TelephonyManager.PHONE_TYPE_GSM) {
            GsmCellLocation gsm = (GsmCellLocation) manager.getCellLocation();
            if (gsm == null) {
                return null;
            }
            if (manager.getNetworkOperator() == null
                    || manager.getNetworkOperator().length() == 0) {
                return null;
            }
            countryCode = Integer.parseInt(manager.getNetworkOperator()
                    .substring(0, 3));
            networkCode = Integer.parseInt(manager.getNetworkOperator()
                    .substring(3, 5));
            areaCode = gsm.getLac();
            CellInfo info = new CellInfo();
            info.cellId = gsm.getCid();
            info.mobileCountryCode = countryCode;
            info.mobileNetworkCode = networkCode;
            info.locationAreaCode = areaCode;
            info.radio_type = "gsm";
            cells.add(info);
            List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
            for (NeighboringCellInfo i : list) {
                CellInfo ci = new CellInfo();
                ci.cellId = i.getCid();
                ci.mobileCountryCode = countryCode;
                ci.mobileNetworkCode = networkCode;
                ci.locationAreaCode = areaCode;
                ci.radio_type = "gsm";
                cells.add(ci);
            }
        } else if (type == TelephonyManager.PHONE_TYPE_CDMA) {
            CdmaCellLocation cdma = (CdmaCellLocation) manager
                    .getCellLocation();
            if (cdma == null) {
                return null;
            }
            if (manager.getNetworkOperator() == null
                    || manager.getNetworkOperator().length() == 0) {
                return null;
            }
            Log.v("TAG", "CDMA");
            CellInfo info = new CellInfo();
            info.cellId = cdma.getBaseStationId();
            info.mobileCountryCode = Integer.parseInt(manager.getNetworkOperator());
            info.mobileNetworkCode = cdma.getSystemId();
            info.locationAreaCode = cdma.getNetworkId();
            info.radio_type = "cdma";
            cells.add(info);
        }
        return cells;
    }

    static class CellInfo {

        public int cellId;
        public int mobileCountryCode;
        public int mobileNetworkCode;
        public int locationAreaCode;

        public String radio_type;

        public CellInfo() {
            super();
        }
    }
}
