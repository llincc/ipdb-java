package net.ipip.ipdb;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import sun.net.util.IPAddressUtil;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class FileDecoder {
    Map<String, Map<String, CityInfo>> netStates = Maps.newHashMap();
    LinkedList<ItemInfo> itemInfos = Lists.newLinkedList();
    private int fileSize;
    private int nodeCount;

    private MetaData meta;
    private byte[] data;

    private int v4offset;

    public FileDecoder(String name) throws IOException, InvalidDatabaseException {
        this(new FileInputStream(new File(name)));
    }

    public FileDecoder(InputStream in) throws IOException, InvalidDatabaseException {
        this.init(this.readAllAsStream(in));
    }

    protected byte[] readAllAsStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    protected void init(byte[] data) throws InvalidDatabaseException {

        this.data = data;
        this.fileSize = data.length;

        long metaLength = bytesToLong(
                this.data[0],
                this.data[1],
                this.data[2],
                this.data[3]
        );
//        {"build":1547017060,"ip_version":1,"languages":{"CN":0},"node_count":411432,"total_size":3330322,"fields":["country_name","region_name","city_name"]}
        byte[] metaBytes = Arrays.copyOfRange(this.data, 4, Long.valueOf(metaLength).intValue() + 4);

        MetaData meta = JSONObject.parseObject(new String(metaBytes), MetaData.class);
        this.nodeCount = meta.nodeCount;
        this.meta = meta;
        System.out.println("nodeCount" + this.nodeCount);
        if ((meta.totalSize + Long.valueOf(metaLength).intValue() + 4) != this.data.length) {
            throw new InvalidDatabaseException("database file size error");
        }

        this.data = Arrays.copyOfRange(this.data, Long.valueOf(metaLength).intValue() + 4, this.fileSize);

        /** for ipv4 */
        if (0x01 == (this.meta.IPVersion & 0x01)) {
            int node = 0;
            for (int i = 0; i < 96 && node < this.nodeCount; i++) {
                if (i >= 80) {
                    node = this.readNode(node, 1);
                } else {
                    node = this.readNode(node, 0);
                }
            }

            this.v4offset = node;
        }
    }

    public String[] find(String addr, String language) throws IPFormatException, InvalidDatabaseException {

        int off;
        try {
            off = this.meta.Languages.get(language);
        } catch (NullPointerException e) {
            return null;
        }

        byte[] ipv;

        if (addr.indexOf(":") > 0) {
            ipv = IPAddressUtil.textToNumericFormatV6(addr);
            if (ipv == null) {
                throw new IPFormatException("ipv6 format error");
            }
            if ((this.meta.IPVersion & 0x02) != 0x02) {
                throw new IPFormatException("no support ipv6");
            }

        } else if (addr.indexOf(".") > 0) {
            ipv = IPAddressUtil.textToNumericFormatV4(addr);
            if (ipv == null) {
                throw new IPFormatException("ipv4 format error");
            }
            if ((this.meta.IPVersion & 0x01) != 0x01) {
                throw new IPFormatException("no support ipv4");
            }
        } else {
            throw new IPFormatException("ip format error");
        }

        int node = 0;
        try {
            node = this.findNode(ipv);
        } catch (NotFoundException nfe) {
            return null;
        }

        final String data = this.resolve(node);
        //数据存储格式 language1 data  language2 data   language3  data
        return Arrays.copyOfRange(data.split("\t", this.meta.Fields.length * this.meta.Languages.size()), off, off + this.meta.Fields.length);
    }

    private int findNode(byte[] binary) throws NotFoundException {

        int node = 0;

        final int bit = binary.length * 8;

        if (bit == 32) {
            node = this.v4offset;
        }

        for (int i = 0; i < bit; i++) {
            if (node > this.nodeCount) {
                break;
            }

            node = this.readNode(node, 1 & ((0xFF & binary[i / 8]) >> 7 - (i % 8))); //这里是 7 - (i%8)
        }

        if (node > this.nodeCount) {
            return node;
        }

        throw new NotFoundException("ip not found");
    }

    private String resolve(int node) throws InvalidDatabaseException {
        final int resoloved = node - this.nodeCount + this.nodeCount * 8;
        if (resoloved >= this.fileSize) {
            throw new InvalidDatabaseException("database resolve error");
        }

        byte b = 0;
        int size = Long.valueOf(bytesToLong(
                b,
                b,
                this.data[resoloved],
                this.data[resoloved + 1]
        )).intValue();

        if (this.data.length < (resoloved + 2 + size)) {
            throw new InvalidDatabaseException("database resolve error");
        }

        try {
            return new String(this.data, resoloved + 2, size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidDatabaseException("database resolve error");
        }
    }

    public int readNode(int node, int index) {
        int off = node * 8 + index * 4;
        //返回第
        return Long.valueOf(bytesToLong(
                this.data[off],
                this.data[off + 1],
                this.data[off + 2],
                this.data[off + 3]
        )).intValue();
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }

    public boolean isIPv4() {
        return (this.meta.IPVersion & 0x01) == 0x01;
    }

    public boolean isIPv6() {
        return (this.meta.IPVersion & 0x02) == 0x02;
    }

    public int getBuildUTCTime() {
        return this.meta.Build;
    }

    public String[] getSupportFields() {
        return this.meta.Fields;
    }

    public String getSupportLanguages() {
        return this.meta.Languages.keySet().toString();
    }

    public void dfs() {
        LinkedList<Integer> list = new LinkedList<>();
        int node = this.v4offset;
        System.out.println(node);
        list.push(0);
        searchNode(node, 0, list);
        list.pop();
        list.push(1);
        searchNode(node, 1, list);
        list.pop();
    }

    private void searchNode(int node, int bt, LinkedList<Integer> list) {
        int newnode = readNode(node, bt);
        if (list.size() > 32) return;
        if (newnode > this.nodeCount) {
            String prefix = getPrefix(list);
            System.out.println(prefix);
            try {
                final String nodeData = resolve(newnode);
                itemInfos.add(new ItemInfo(prefix, nodeData));
//                String[] languagesData = nodeData.split("\t", this.meta.Fields.length * this.meta.Languages.size());
//
//
//                Map<String, CityInfo> languageMap = Maps.newHashMap();
//                for (Map.Entry<String, Integer> entry : this.meta.Languages.entrySet()) {
//                    String language = entry.getKey();
//                    int off = entry.getValue();
//                    String fields[] = Arrays.copyOfRange(languagesData, off, off + this.meta.Fields.length);
//                    CityInfo cityInfo = new CityInfo(fields);
//
//                    languageMap.put(language, cityInfo);
//                }
//                netStates.put(prefix, languageMap);
            } catch (Exception e) {
                System.out.println("out of boundary");
                return;
            } finally {
                return;
            }
        }
        list.push(0);
        searchNode(newnode, 0, list);
        list.pop();
        list.push(1);
        searchNode(newnode, 1, list);
        list.pop();

    }

    public static String getPrefix(LinkedList<Integer> list) {
        int var1 = 0;
        int var2 = 0;
        int var3 = 0;
        int var4 = 0;
        int len = list.size();
        for (int i = 0; i < 8 && i < len; i++) {
            var1 = (var1 << 1) | list.get(i);
        }
        if (len < 8) var1 <<= 8 - len;
        for (int i = 8; i < 16 && i < len; i++) {
            var2 = (var2 << 1) | list.get(i);
        }
        if (len > 8 && len < 16) var2 <<= 16 - len;
        for (int i = 16; i < 24 && i < len; i++) {
            var3 = (var3 << 1) | list.get(i);
        }
        if (len > 16 && len < 24) var3 <<= 24 - len;
        for (int i = 24; i < 32 && i < len; i++) {
            var4 = (var4 << 1) | list.get(i);
        }
        if (len > 24 && len < 32) var4 <<= 32 - len;
        return String.format("%d.%d.%d.%d/%d", var1, var2, var3, var4, len);
    }

    public void toFile(String outfilename) throws Exception{
        System.out.println(this.itemInfos.size());
        BufferedWriter bw = null;
        FileWriter fw = new FileWriter(outfilename);
        bw = new BufferedWriter(fw);
        for (ItemInfo itemInfo : itemInfos) {
            bw.write(itemInfo.toString());
        }
        if (bw != null) {
            bw.close();
        }
        fw.close();
    }

    public static void main(String[] args) throws Exception {
//        FileDecoder fileDecoder = new FileDecoder("/Users/linleicheng/Code/ipdb-java/ipv4_pro.ipdb");
//        System.out.println(JSONObject.toJSONString(fileDecoder.meta, SerializerFeature.WriteNullStringAsEmpty));
//        System.out.println(fileDecoder.v4offset);
//        System.out.println(fileDecoder.readNode(fileDecoder.v4offset, 0));

        City city = new City("/Users/linleicheng/Code/ipdb-java/ipv4_pro.ipdb");
        System.out.println(city.findInfo("123.123.123.123", "CN"));
//        String[] data = fileDecoder.find("113.210.98.70", "CN");
//        CityInfo info = new CityInfo(data);
//        System.out.println(info.toString());
//        fileDecoder.dfs();
//        fileDecoder.toFile("/Users/linleicheng/Code/ipdb-java/data.txt");
    }
}
