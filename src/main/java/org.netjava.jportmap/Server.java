package org.netjava.jportmap;
import java.net.*;
import java.util.*;

/**
 * <p>Title: 端口转发器</p>
 * <p>Description:启动监听服务 </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: www.NetJava.org</p>
 * @author javafound
 * @version 1.0
 */
public class Server extends Thread {
    //创建一个转发服务器
    public Server(Route route, int id) {
        this.route = route;
        connectionQueue = new Vector();
        myID = id;
        start();
    }

    //关闭这个服务器：
    public void closeServer() {
        isStop = true;
        if (null != myServer) {
            closeServerSocket();
        } while (this.connectionQueue.size() > 0) {
            Transfer tc = (Transfer) connectionQueue.remove(0);
            tc.closeSocket(tc.socket);
            tc = null;
        }
    }

    //启动转发服务器的执行线程
    public void run() {
        SysLog.info(" start Transfer......:" + route.toString());
        ServerSocket myServer = null;
        try {
            InetAddress myAD = Inet4Address.getByName(route.LocalIP);
            myServer = new ServerSocket(route.LocalPort, 4, myAD);
        } catch (Exception ef) {
            SysLog.severe("Create Server " + route.toString() + " error:" + ef);
            closeServerSocket();
            return;
        }
        SysLog.info("Transfer Server : " + route.toString() + " created OK");
        while (!isStop) {
            String clientIP = "";
            try {
                Socket sock = myServer.accept();
                clientIP = sock.getInetAddress().getHostAddress();
                if (checkIP(route, clientIP)) {
                    SysLog.warning(" ransfer Server : " + route.toString() +
                            "  Incoming:" + sock.getInetAddress());
                    sock.setSoTimeout(0);
                    connCounter++;
                    Transfer myt = new Transfer(sock, route);
                    connectionQueue.add(myt);
                } else {
                    SysLog.warning(" ransfer Server : " + route.toString() +
                            "  Refuse :" + sock.getInetAddress());
                    closeSocket(sock);
                }

            } catch (Exception ef) {
                SysLog.severe(" Transfer Server : " + route.toString() +
                        " accept error" + ef);
            }
        }
    }

    //检测进入的IP是否己许可
    private static boolean checkIP(Route route, String inIP) {
        String[] inI = string2StringArray(inIP, ".");
        String[] list = string2StringArray(route.AllowClient, ".");
        if (inI.length != list.length) {
            SysLog.severe(" Transfer Server Error Cfg AllowClient : " +
                    route.toString());
            return false;
        }
        for (int i = 0; i < inI.length; i++) {
            if ((!inI[i].equals(list[i])) && !(list[i].equals("*"))) {
                System.out.println(": " + inI[i] + " :" + list[i]);
                return false;
            }
        }
        return true;
    }


    /*
     * @param srcString 原字符串
     * @param separator 分隔符
     * @return 目的数组
     */
    private static final String[] string2StringArray(String srcString,
                                                     String separator) {
        int index = 0;
        String[] temp;
        StringTokenizer st = new StringTokenizer(srcString, separator);
        temp = new String[st.countTokens()];
        while (st.hasMoreTokens()) {
            temp[index] = st.nextToken().trim();
            index++;
        }
        return temp;
    }

    //关闭ServerSocket
    private void closeServerSocket() {
        try {
            this.myServer.close();
        } catch (Exception ef) {
        }
    }
    private void closeSocket(Socket s) {
        try {
            s.close();
        } catch (Exception ef) {

        }
    }

    //服务器
    private ServerSocket myServer = null;
    //连结队列控制
    private boolean isStop = false;
    //
    private Vector connectionQueue = null;
    private int connCounter = 0;
    // 路由对象
    private Route route = null;
    //连结的ID号，暂未用
    private static int  myID = 0;
}
