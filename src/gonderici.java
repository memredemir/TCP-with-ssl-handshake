import java.io.*;
import java.security.*;
import javax.net.ssl.*;

public class gonderici 
{
    public static void main(String[] args) throws InterruptedException 
    {
        SSLServerSocket serverSock = null;
        SSLSocket socket = null;
        PrintWriter out = null;
        try {
            //Bu keystore hangi tip dosya saklanacagini belirtiliyor.
            KeyStore serverKeys = KeyStore.getInstance("JKS");
            
            //privateserver.jks dosyasi bu keystore'a yuklenir. Bu jks'yi acmak icin gereken parolada yazilir.
            serverKeys.load(new FileInputStream("server.private"), "atacanemre".toCharArray());
            
            //Hangi sertifika tipi olacagi belirtilir.
            KeyManagerFactory serverKeyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            
            //privateserver.jks icindeki entry'e ulasmamiz lazim. Bunun icin bu entry'i acicak olan parolayi belirtiyoruz.
            serverKeyManager.init(serverKeys, "atacanemre".toCharArray());

            //Clientdan gelen jks dosyasinin icerisindeki sertifikayi guvenilir olarak belirtmek icin once java
            //yapısında bir keystore'a yukluyoruz.
            KeyStore clientPub = KeyStore.getInstance("JKS");

            clientPub.load(new FileInputStream("client.public"), "atacanemre".toCharArray());
            
            //Sertifika tipi belirtildi.
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManager.init(clientPub);

            // Yukarida tanimlanan yapilar kullanilarak SSL socket initialize edilir.
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(serverKeyManager.getKeyManagers(),
                    trustManager.getTrustManagers(),
                    SecureRandom.getInstance("SHA1PRNG"));
            
            serverSock = (SSLServerSocket) ssl.getServerSocketFactory().createServerSocket(8889);
            
            //SSLServerSocket Client icin kimlik dogrulamasi gerektirecek sekilde yapilandirilir
            serverSock.setNeedClientAuth(true);
            
        } catch (Exception e) {}
        
        while (true) {
            System.out.println("***** SERVER ISTEK BEKLIYOR *****");
            Thread.sleep(10000);
            try {
                //Handshake accept edilir
                socket = (SSLSocket) serverSock.accept();
                
                try {
                    //Client'dan gelen sertifika alinir ve icerigi goruntulenir.
                    SSLSession session = socket.getSession();
                    java.security.cert.Certificate[] clientcerts = session.getPeerCertificates();
                    for (int i = 0; i < clientcerts.length; i++) {
                        System.out.println("-Public Key-");
                        System.out.println(clientcerts[i].getPublicKey());
                        System.out.println("-Certificate Type-");
                        System.out.println(clientcerts[i].getType());
                    }
                } catch (Exception e) {}
                
                //Gönderilecek dosya adi
                String filename = "/Users/emre/Desktop/asdas.jpg";

                //Dosya adi Client' a gonderilir
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                out.println(filename);
                out.flush();

                File transferFile = new File(filename);

                byte [] dizi =new byte [(int)transferFile.length()];
    			
    			FileInputStream file_IStream =new FileInputStream(transferFile);
    			BufferedInputStream buff_IStream =new BufferedInputStream(file_IStream);
    			buff_IStream.read(dizi,0,dizi.length); 
    			
    			OutputStream oStream1 =socket.getOutputStream(); 
    			oStream1.write(dizi,0,dizi.length); //dosyayi yolladik
    			oStream1.flush();
    			socket.close(); //socketi kapattik
    			
    			System.out.println("***** GONDERME ISLEMI TAMAMLANDI *****");

            } catch (Exception e) {
                System.out.println("***** HANDSHAKE BASARISIZ OLDU BAGLANTI SONLANDIRILDI *****");
            } finally {
                if (out != null)
                    out.close();
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {}
            }
        }
    }
}
