/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import httpserver.HttpServerXL.GetPageHandler.GetParamHandler;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpServerXL {

    private static HttpServer ServerSocket;

    public static void Start() throws IOException {
        ServerSocket = HttpServer.create(new InetSocketAddress(7000), 0);
        ServerSocket.createContext("/info", new InfoHandler());
        ServerSocket.createContext("/get", new GetHandler());
        ServerSocket.createContext("/page", new GetPageHandler());
        ServerSocket.createContext("/param", new GetParamHandler());
        ServerSocket.setExecutor(null);//create default extructur
        ServerSocket.start();
    }

    static class InfoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange HandleResponse) throws IOException {

            BufferedReader in = new BufferedReader(new FileReader("D:\\mypage.txt"));
            String str = "";
            String line = "";
            while ((line = in.readLine()) != null) {
                str += in.readLine();

            }
            System.out.println(str);

            String ResponseText = "Fatih Sultan Mehmet Vakif University";
            HandleResponse.sendResponseHeaders(500, str.getBytes().length);//headeri ayarlıyoruz
            OutputStream os = HandleResponse.getResponseBody(); //body referansını alıyoruz
            os.write(str.getBytes());//cevabı yazdırıyoruz
            os.close();
        }
    }

    static class GetHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange HandleResponse) throws IOException {
            //pdf göndereceğiz
            Headers h = HandleResponse.getResponseHeaders();
            h.add("Content-Type", "application/pdf"); //dosya tipini belirtiyoruz

            File file = new File("C:\\deneme.pdf"); //pdf yolu dosyayı alıyoruz
            byte[] bytearray = new byte[(int) file.length()];//bir buffer tanımlıyoruz
            FileInputStream fis = new FileInputStream(file);//dosyayı byte arraya çevireceğiz
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);//dosyayı byte arraya çevirdik

            //ceap için hazırız
            HandleResponse.sendResponseHeaders(200, file.length());//cevap için header ayarlandı
            OutputStream os = HandleResponse.getResponseBody(); //body referansı alıyoruz
            os.write(bytearray, 0, bytearray.length);//dosya data kısmına byte dizisi olarak eklendi ve gönderildi
            os.close();
        }

    }

    static class GetPageHandler implements HttpHandler {

        String str;
        //@content('slavepagename') tag i ile belirtilen slavepagename kodlarını replace yapar
        public void content() throws FileNotFoundException, IOException {
            String line = ""; //hedefteki dosyanın içeriğini yazacağımız string
            //slavepage ile @content tag i içerisine yazılacak slave page alınır
            //str.indexOf("@content('") + 10 tag in indexi alınır +10 ile tag i almıyoruz begin index i belirlemiş olduk
            //str.indexOf("')", str.indexOf("@content('")) edn index te ise tagden sonraki kısım ile kapama parantez arası alınır
            String slavepage = str.substring(str.indexOf("@content('") + 10, str.indexOf("')", str.indexOf("@content('")));
            //alınan slave page dosyası okunur 
            BufferedReader in = new BufferedReader(new FileReader("D:\\" + slavepage));
            //okunan line lar slavecontent e atılır
            String slavecontent = "";
            while ((line = in.readLine()) != null) {
                slavecontent += line;

            }
            //kodlar ile @content('slavepagename') replace edilir
            str = str.replace("@content('" + slavepage + "')", slavecontent);
            //BufferReader kapanır
            in.close();

        }
           //@master('masterpagename') tag i ile belirtilen masterpagename kodlarını replace yapar
           //content() metodu ile benzer
        public void master() throws FileNotFoundException, IOException {
            String line = "";
            String masterpage = str.substring(str.indexOf("@master('") + 9, str.indexOf("')", str.indexOf("@master('")));
            BufferedReader in = new BufferedReader(new FileReader("D:\\" + masterpage));
            String mastercontent = "";
            while ((line = in.readLine()) != null) {
                mastercontent += line;

            }
            str = str.replace("@master('" + masterpage + "')", mastercontent);
            in.close();
        }
        //@for(number,slavepagename)  number kadar slavepagename dosyasının içindeki kodları tekrarlar 
        public void For() throws FileNotFoundException, IOException {
            //hedefteki dosyanın içeriğini yazacağımız string
            String line = "";
            //slave page nin index ini alır.
            int indexOfSlavePage = str.indexOf(",", str.indexOf("@for("));
            //tekrarlanan slavePageName i alır
            String slavepage = str.substring(indexOfSlavePage + 1, str.indexOf(")", indexOfSlavePage));
            //number ı alır . @for( tag inden , slave page nin numarasına kadar ki kısım yani tekrar sayısını alır
            int number = Integer.parseInt(str.substring(str.indexOf("@for(") + 5, indexOfSlavePage));

            BufferedReader in = new BufferedReader(new FileReader("D:\\" + slavepage));
            String slavecontent = "";
            while ((line = in.readLine()) != null) {
                slavecontent += line;

            }
            in.close();
            String replaceText = "";
            //tekrar sayısı kadar yazdırma işlemi yapılır
            for (int i = 0; i < number; i++) {
                replaceText += slavecontent + "\n";
            }
            //for tag i forText e atılır
            String forText = str.substring(str.indexOf("@for("), str.indexOf(")", indexOfSlavePage) + 1);
            //replace edilir
            str = str.replace(forText, replaceText);

        }

        @Override
        public void handle(HttpExchange HandleResponse) throws IOException {
            //text dosyası göndereceğiz
            Headers h = HandleResponse.getResponseHeaders(); // göndereceğimiz dosya tipini seçiyotruz
            h.add("Content-Type", "text/html"); //header ayarlaması yapıyoruz
        
            BufferedReader in = new BufferedReader(new FileReader("D:\\mypage.txt"));
            str = "";
            String lineq = "";
            //belirtilen dosyayı okuyoruz
            while ((lineq = in.readLine()) != null) {
                str += lineq + "\n";

            }
            //okunan satır "@content" içeriyorsa content() metoduna girer 
            while (str.contains("@content")) {
                content();
            }  
            //okunan satır "@master" içeriyorsa master() metoduna girer 
            while (str.contains("@master")) {
                master();
            }
            //okunan satır "@for" içeriyorsa for() metoduna girer 
            while (str.contains("@for(")) {
                For();
            }

            //cevap için hazırız
            HandleResponse.sendResponseHeaders(500, str.getBytes().length);//headeri ayarlıyoruz
            OutputStream os = HandleResponse.getResponseBody(); //body referansını alıyoruz
            os.write(str.getBytes());//cevabı yazdırıyoruz
            os.close();
        }

        static class GetParamHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange HandleResponse) throws IOException {
                StringBuilder response = new StringBuilder();
                Map<String, String> parms = HttpServerXL.queryToMap(HandleResponse.getRequestURI().getQuery());
                response.append("<html><body>");
                response.append("hello : " + parms.get("hello") + "<br/>");
                response.append("foo : " + parms.get("hello") + "<br/>");
                response.append("<div> <p style = \"color : #ffffff; background-color:#ff000\">Text color : white, background-color");
                response.append("</body></html>");

                HandleResponse.sendResponseHeaders(200, response.length());
                OutputStream os = HandleResponse.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            }

        }
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        HttpServerXL.Start();
    }

}
