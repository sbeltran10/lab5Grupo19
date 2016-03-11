package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorUbicacion {

	/**
	 * Constante que especifica el tiempo m�ximo en milisegundos que se esperara 
	 * por la respuesta de un cliente en cada una de las partes de la comunicaci�n
	 */
	private static final int TIME_OUT = 10000;
	
	/**
	 * Constante que especifica el numero de threads que se usan en el pool de conexiones.
	 */
	public static final int N_THREADS = 4;

	/**
	 * Puerto en el cual escucha el servidor.
	 */
	public static final int PUERTO_TCP = 8080;
	public static final int PUERTO_UDP = 8081;
	
	/**
	 * Id de los clientes
	 */
	public int id;

	/**
	 * Metodo main del servidor con seguridad que inicializa un 
	 * pool de threads determinado por la constante nThreads.
	 * @param args Los argumentos del metodo main (vacios para este ejemplo).
	 * @throws IOException Si el socket no pudo ser creado.
	 */
	public static void main(String[] args) throws IOException {
		new ServidorUbicacion().iniciarCom();
	}

	/**
	 * Metodo que atiende a los usuarios.
	 */
	public void iniciarCom() {
		id = 0;
		final ExecutorService poolTCP = Executors.newFixedThreadPool(N_THREADS);
		final ExecutorService poolUDP = Executors.newFixedThreadPool(N_THREADS);
		Runnable serverRunTCP = new Runnable(){

			@Override
			public void run() {
				ServerSocket servidorSocket = null;
				try{
					servidorSocket = new ServerSocket(PUERTO_TCP);
					System.out.println("Listo para recibir conexiones TCP");
					while(true){
						Socket cliente = servidorSocket.accept();

						cliente.setSoTimeout(TIME_OUT);
					
						poolTCP.execute(new ComunicacionTCP(cliente));
					}
				}catch(Exception e){
					System.err.println("Ocurrio un error");
					e.printStackTrace();
				}finally{
					try{
						servidorSocket.close();
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};

		Runnable serverRunUDP = new Runnable(){

			@Override
			public void run() {
				DatagramSocket servidorSocket = null;
				try{
					servidorSocket = new DatagramSocket(PUERTO_UDP);
					System.out.println("Listo para recibir conexiones UDP");
					while(true){
						byte[] buf = new byte[256];
						DatagramPacket cliente = new DatagramPacket(buf, buf.length);
						servidorSocket.receive(cliente);
						id++;
						poolUDP.execute(new ComunicacionUDP(servidorSocket, cliente, id));
					}
					
				}catch(Exception e){
					System.err.println("Ocurrio un error");
					e.printStackTrace();
				}finally{
					try{
						servidorSocket.close();
					}
					catch(Exception e){
						e.printStackTrace();
					}

				}
			}
		};

		Thread serverTCP = new Thread(serverRunTCP);
		serverTCP.start();

		Thread serverUDP = new Thread(serverRunUDP);
		serverUDP.start();

	}


}
