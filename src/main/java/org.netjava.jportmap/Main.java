package org.netjava.jportmap;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * <p>Title: 端口转发器</p>
 * <p>Description:启动主类：读取配置，启动监听服务 </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: www.NetJava.org</p>
 * @author javafound
 * @version 1.0
 */
public class Main {

    //Server服务器集合
    private static List<Server> serverList = new ArrayList();

    //Route集合
    private static List<Route> routeList = new ArrayList();

    //start......
    public static void main(String args[]) {
        startService();
    }

    //start
    public static void startService() {
        if (!loadCfgFile()) {
            System.exit(1);
        }
        while (serverList.size() > 0) {
            Server ts =   serverList.remove(0);
            ts.closeServer();
        }
        for (int i = 0; i < routeList.size(); i++) {
            Route r = routeList.get(i);
            Server server = new Server(r, i);
            serverList.add(server);
        }
    }


    // 停止服务接口,备用其它模块调用
    public static void stop() {
        while (serverList.size() > 0) {
            Server ts = serverList.remove(0);
            ts.closeServer();
        }

    }
    /**
     *从配置文件读取数据，生成Route对象
     * read cfg parameter
     * @return boolean
     */
    private static boolean loadCfgFile() {
        try {
            String userHome = System.getProperties().getProperty("user.dir");
            if (userHome == null) {
                userHome = "";
            } else {
                userHome = userHome + File.separator;
            }
            userHome += "cfg" + File.separator + "jPortMap.cfg";
            InputStream is = new FileInputStream(userHome);
            Properties pt = new Properties();
            pt.load(is);
            //共有几个业务模块
            int ServiceCount = Integer.parseInt(pt.getProperty("TransferCount"));
            for (; ServiceCount > 0; ServiceCount--) {
                Route r = new Route();
                r.LocalIP = pt.getProperty("LocalIP." + ServiceCount).trim();
                r.LocalPort = Integer.parseInt(pt.getProperty("LocalPort." +
                        ServiceCount).trim());
                r.DestHost = pt.getProperty("DestHost." + ServiceCount).trim();
                r.DestPort = Integer.parseInt(pt.getProperty("DestPort." +
                        ServiceCount).trim());
                r.AllowClient = pt.getProperty("AllowClient." + ServiceCount).
                        trim();
                routeList.add(r);
            }
            is.close();
            SysLog.info("ystem Read cfg file OK");
        } catch (Exception e) {
            System.out.println("找不到配置文件:"+e);
            SysLog.severe("loadCfgFile false :" + e);
            return false;
        }
        return true;
    }

}



