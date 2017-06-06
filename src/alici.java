import java.io.*;
import java.security.*;

import javax.net.ssl.*;

public class alici 
{
	public static void main(String[] args) throws IOException 
	{
		int okunan_byte = 0;
		int filesize=102238600;
		int totNow = 0;
		SSLSocket socket = null;
		BufferedReader b_in = null;

		BufferedReader b_reader = new BufferedReader(
				new InputStreamReader(System.in));

		 System.out.println("Lutfen baglanmak istediginiz sunucunun ip numarasını giriniz!!");
		 String ip = b_reader.readLine();
		//String ip = "localhost";

		try {
			
			KeyStore c_keys = KeyStore.getInstance("JKS");//Dosya tipinin jks seklinde saklandigini belirttik.
 
			c_keys.load(new FileInputStream("client.private"),"atacanemre".toCharArray());//client.private dosyası ve jks yi acmak icin gereken parola bu keystore'a yuklenir.

			KeyManagerFactory c_key_man = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//Sertifika tipi.

			c_key_man.init(c_keys, "atacanemre".toCharArray());//client.private icindeki entry'e ulasmamiz icin gereken parolayi belirttik.
			
			
			KeyStore s_public = KeyStore.getInstance("JKS");

			s_public.load(new FileInputStream("server.public"),"atacanemre".toCharArray());
			
			TrustManagerFactory trust_man = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//Sertifika tipi.

			trust_man.init(s_public);

			SSLContext ssl = SSLContext.getInstance("SSL");// SSL socketi yukardaki tanimlara gore tanimladik.

			ssl.init(c_key_man.getKeyManagers(),trust_man.getTrustManagers(),SecureRandom.getInstance("SHA1PRNG"));

			socket = (SSLSocket) ssl.getSocketFactory().createSocket(ip, 15022);

			try 
			{
				socket.startHandshake();//Handshake baslangici.
			}
			
			catch (Exception e) 
			{
				System.out.println("***** Handshake yapilamadi! ******");
				System.exit(0);
			}
			
				// receive data
				b_in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String dosyaAdi = b_in.readLine(); // Dosya adini aldik
				
				byte [] dizi = new byte [filesize]; 
				
				InputStream istream = socket.getInputStream(); 
				dosyaAdi = dosyaAdi.substring(0, dosyaAdi.indexOf('.')) + "_alindi" + dosyaAdi.substring(dosyaAdi.indexOf('.'));
				FileOutputStream file_ostream = new FileOutputStream(dosyaAdi); // yeni ismi ile yeni dosya yarattik
				
				BufferedOutputStream buff_ostream = new BufferedOutputStream(file_ostream); 
				
				okunan_byte = istream.read(dizi,0,dizi.length); 
				
				totNow = okunan_byte; 
				
				
				do 
				{ // gelen mesaji byte byte okuduk
				
					okunan_byte = istream.read(dizi, totNow, (dizi.length-totNow));
					
					if(okunan_byte >= 0) 
					
						totNow=totNow + okunan_byte; 
					
				} while(okunan_byte > -1); 
					
				buff_ostream.write(dizi, 0 , totNow); // dosyaya yazdik
					
				buff_ostream.flush();
					
				buff_ostream.close(); // dosyayi kapattik
				
				
				System.out.println("*****DOSYA TRANSFERI BASARILI BICIMDE TAMAMLANDI*****");
				socket.close();  // socket i kapattik
		}
		
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		finally 
		{
			try 
			{
				if (b_in != null)
					b_in.close();
			}			
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}