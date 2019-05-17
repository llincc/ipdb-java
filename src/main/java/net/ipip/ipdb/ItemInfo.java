package net.ipip.ipdb;

import java.util.Map;

public class ItemInfo {
    private String pfx;
    private String data;
    public ItemInfo(String pfx, String data){
        this.pfx = pfx;
        this.data = data;
    }

    @Override
    public String toString() {
        return pfx+"\t"+data+"\n";
    }
}

//class Item{
//    private String language;
//
//    private String[] data;
//
//    private int size;
//
//    public Item(String[] data) {
//        this.data = data;
//        this.size = data.length;
//    }
//
//    private String get(int index) {
//        return this.size >= index ? this.data[index-1] : "";
//    }
//
//    public String getCountryName() {
//        return this.get(1);
//    }
//
//    public String getRegionName() {
//        return this.get(2);
//    }
//
//    public String getCityName() {
//        return this.get(3);
//    }
//
//    public String getOwnerDomain() {
//        return this.get(4);
//    }
//
//    public String getIspDomain() {
//        return this.get(5);
//    }
//
//    public String getLatitude() {
//        return this.get(6);
//    }
//
//    public String getLongitude() {
//        return this.get(7);
//    }
//
//    public String getTimezone() {
//        return this.get(8);
//    }
//
//    public String getUtcOffset() {
//        return this.get(9);
//    }
//
//    public String getChinaAdminCode() {
//        return this.get(10);
//    }
//
//    public String getIddCode() {
//        return this.get(11);
//    }
//
//    public String getCountryCode() {
//        return this.get(12);
//    }
//
//    public String getContinentCode() {
//        return this.get(13);
//    }
//
//    public String getIDC() {
//        return this.get(14);
//    }
//
//    public String getBaseStation() {
//        return this.get(15);
//    }
//
//    public String getCountryCode3() {
//        return this.get(16);
//    }
//
//    public String getEuropeanUnion() {
//        return this.get(17);
//    }
//
//    public String getCurrencyCode() {
//        return this.get(18);
//    }
//
//    public String getCurrencyName() {
//        return this.get(19);
//    }
//
//    public String getAnycast() {
//        return this.get(20);
//    }
//
//    @Override
//    public String toString() {
//
//        StringBuffer sb = new StringBuffer();
//        sb.append("\t");
//        sb.append("language:");
//        sb.append(this.language);
//        sb.append("\t");
//        sb.append("country_name:");
//        sb.append(this.getCountryName());
//        sb.append("\t");
//        sb.append("region_name:");
//        sb.append(this.getRegionName());
//        sb.append("\t");
//        sb.append("city_name:");
//        sb.append(this.getCityName());
//        sb.append("\t");
//        sb.append("owner_domain:");
//        sb.append(this.getOwnerDomain());
//        sb.append("\t");
//        sb.append("isp_domain:");
//        sb.append(this.getIspDomain());
//        sb.append("\t");
//        sb.append("latitude:");
//        sb.append(this.getLatitude());
//        sb.append("\t");
//        sb.append("longitude:");
//        sb.append(this.getLongitude());
//        sb.append("\t");
//
//        sb.append("timezone:");
//        sb.append(this.getTimezone());
//        sb.append("\t");
//
//        sb.append("utc_offset:");
//        sb.append(this.getUtcOffset());
//        sb.append("\t");
//
//        sb.append("china_admin_code:");
//        sb.append(this.getChinaAdminCode());
//        sb.append("\t");
//
//        sb.append("idd_code:");
//        sb.append(this.getIddCode());
//        sb.append("\t");
//
//        sb.append("country_code:");
//        sb.append(this.getCountryCode());
//        sb.append("\t");
//
//        sb.append("continent_code:");
//        sb.append(this.getContinentCode());
//        sb.append("\t");
//
//        sb.append("idc:");
//        sb.append(this.getIDC());
//        sb.append("\t");
//
//        sb.append("base_station:");
//        sb.append(this.getBaseStation());
//        sb.append("\t");
//
//        sb.append("country_code3:");
//        sb.append(this.getCountryCode3());
//        sb.append("\t");
//
//        sb.append("european_union:");
//        sb.append(this.getEuropeanUnion());
//        sb.append("\t");
//
//        sb.append("currency_code:");
//        sb.append(this.getCurrencyCode());
//        sb.append("\t");
//
//        sb.append("currency_name:");
//        sb.append(this.getCurrencyName());
//        sb.append("\t");
//
//        sb.append("anycast:");
//        sb.append(this.getAnycast());
//
//        return sb.toString();
//    }
//}
