import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerWorker implements Runnable{
<<<<<<< HEAD
    public InetAddress[] ips;
    public Security s;
=======
>>>>>>> 1254c7e32672f57ef3c5b113fa3a0ff9b5e01a70
    public BufferedWriter myWriter;
    public BufferedWriter http_info;
    private DatagramPacket received;
    private DatagramSocket socket;
    private File folder;
    private int window;


    public void whenWriteStringUsingBufferedWritter_thenCorrect() throws IOException {
        this.myWriter = new BufferedWriter(new FileWriter("Logs",true));
        this.http_info = new BufferedWriter(new FileWriter("http",true));
        this.myWriter.write("Logs:\n");
    }


    public ServerWorker(DatagramPacket received,File folder) {
        this.received = received;
        this.folder = folder;
        
        try {
            whenWriteStringUsingBufferedWritter_thenCorrect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.window = 1;
    }

    public void run() {
        try {
            socket.setSoTimeout(1000);
            int i = 0;
            // Vemos a informação do cliente no Packet.
            int port = this.received.getPort();
            InetAddress clientIP = this.received.getAddress();
            while (i < 10){
                try {

                    this.s = new Security();
                    boolean authenticity = s.verifyPacketAuthenticity(this.received.getData());

                    if (authenticity) {
                        byte[] packet = this.received.getData();
                        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                        
                        // Lemos o opcode que veio no Packet.
                        int opcode = bis.read();

                        // Se opcode == 1 , enviamos a informaçao dos ficheiros para o cliente e no fim,enviamos um FINPacket.
                        if (opcode == 1) {
                            sendFileInfo(clientIP,port);
                        }
                        // Se opcode == 2, recebemos um pedido de leitura de um ficheiro. Enviamos o ficheiro ao cliente e no fim enviamos um FINPacket.
                        if (opcode == 2) {
                            ReadFilePacket readFile = ReadFilePacket.deserialize(bis);
                            System.out.print("Pedido para enviar o ficheiro " + readFile.getFileName());
                            sendFile(readFile,clientIP,port);
                        }
                        //Se opcode == 5, recebemos um FINPacket. Isso significa que já enviamos um FINPacket e assim saímos.
                        if (opcode == 5) {
                            i = 10;
                        }
                        // Criar um novo pacote e esperar pela resposta do cliente.
                        byte[] indata = new byte[1320];
                        this.received = new DatagramPacket(indata,1320);
                        socket.receive(this.received);
                    }
                }
                catch (SocketTimeoutException e) {
                    i++;
                }
            }
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Envia ao cliente a informação dos ficheiros na sua diretoria a sincronizar.
    public void sendFileInfo(InetAddress ip,int port) throws IOException{
        //Abrir stream onde escrevemos os bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int numB = 1;
        //Percorremos o array dos ficheiros, vemos a sua informação e escrevemos no stream;
        List<File> fSend = escolherFicheirosEnviar(folder);
        List<DataTransferPacket> fiWindow = new ArrayList<>();
        int i = 0;
        for (File f  : fSend) {
            String filename = f.getAbsolutePath();
            //Criar o path relativo para o ficheiro.
            Path file = Path.of(filename);
            Path parent = Path.of(folder.getAbsolutePath());
            file = parent.relativize(file);
            FileInfo fi = new FileInfo(file.toString(),Long.toString(f.lastModified()));
            // Se a informação do ficheiro já nao tiver espaço no pacote, entao enviamos o pacote e começamos um novo onde escrevemos a informaçao do ficheiro.
            if ((bos.size() + fi.serialize().length + 1) > 1293) {
                bos.write(0);
                byte[] data = bos.toByteArray();
                DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
                i++;
                fiWindow.add(fileInfos);
                //sendDataPacket(fileInfos, ip, port);
                bos.close();
                bos = new ByteArrayOutputStream();
            }
            bos.write('+');
            bos.write(fi.serialize());


            if (window == i) {
                sendDataPacket(fiWindow, ip, port);
                fiWindow = new ArrayList<>();
                i = 0;
            }
        }
        // O byte 0 indica que não temos mais dados;
        bos.write(0);
        byte[] data = bos.toByteArray();
        DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
        fiWindow.add(fileInfos);
        sendDataPacket(fiWindow, ip, port);
        FINPacket fin = new FINPacket();
        byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
        DatagramPacket finPacket = (new DatagramPacket(packetToSend,packetToSend.length,ip,port));
        socket.send(finPacket);
    }

    private List<File> escolherFicheirosEnviar(File pasta) {
        List<File> res = new ArrayList<>();
        File[] subFicheiros = pasta.listFiles();
        for (File f  : subFicheiros) {
            if (!f.isHidden()) {
                if (f.isDirectory())
                    verFicheirosPasta(f, res);
                else {
                    res.add(f);
                }
            }
        }
        return res;
    }

    private void verFicheirosPasta(File pasta,List<File> res) {
        File[] ficheirosPasta = pasta.listFiles();
        for (File f  : ficheirosPasta) {
            if (!f.isHidden()) {
                if (f.isDirectory())
                    verFicheirosPasta(f, res);
                else {
                    res.add(f);
                }
            }
        }
    }


    // Envia ficheiro ao cliente, verificando sempre que este recebe cada bloco de dados.
    public void sendFile(ReadFilePacket readFile,InetAddress clientIP,int port) throws IOException{
        String f = readFile.getFileName();
        long size = 0;
        InetAddress host = InetAddress.getLocalHost();
        Path file = Path.of(folder.getAbsolutePath()).resolve(f);
        System.out.println("A enviar o ficheiro " + file.toString());
        this.myWriter.append("A enviar o ficheiro " + file.toString()+ "\n");
        this.http_info.append("A enviar o ficheiro " + file.toString() + "\n" + "IP: " + host.toString() + "  -------->  " + "IP: " + clientIP.toString() + "\n");
        // Verificar que estao a pedir um ficheiro existente.
        File ficheiro = new File(file.toString());
        if (!ficheiro.exists()) {
            this.myWriter.append("Ficheiro nao existe \n");

            return;
        }
        FileInputStream fis = new FileInputStream(ficheiro);
        int numB = 1;
        // Enquanto houver bytes para ler, enviamos os dados num DataTransferPacket.
        while (fis.available() != 0) {
            int i = 0;
            List<DataTransferPacket> dtFileWindow = new ArrayList<>();
            while (window > i) {
                if (fis.available() != 0) {
                    byte[] fileData = fis.readNBytes(1293);
                    DataTransferPacket dtFile = new DataTransferPacket(numB++, fileData.length, fileData);
                    dtFileWindow.add(dtFile);
                    size += fileData.length;
                }
                i++;
            }
            sendDataPacket(dtFileWindow, clientIP, port);
        }
        System.out.println("Ficheiro acabado de enviar");
        this.myWriter.append("Ficheiro acabado de enviar\n");
        this.myWriter.close();
        FINPacket fin = new FINPacket();
        byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
        DatagramPacket finPacket = (new DatagramPacket(packetToSend,packetToSend.length,clientIP,port));
        socket.send(finPacket);
        fis.close();
        this.http_info.append("Debito :" + "\n");
        this.http_info.append("Enviado com sucesso " + size + " Bytes\n");
        this.http_info.close();
        
    }

    // Envia um DataTransferPacket para o ip e port designados.
    public void sendDataPacket (List<DataTransferPacket> data,InetAddress ip, int port) throws IOException{
        boolean verificado = false;
        int i = 0;
        socket.setSoTimeout(1000);
        int numB = data.get(0).getNumBloco();
        int enviados = 0;
        while (i < 5) {
            try {
                // Enquando nao se verificar que o cliente recebeu o pacote, enviamos o pacote.
                while (!verificado && enviados < data.size()) {
                    int atual = enviados;
                    verificado = false;
                    numB = data.get(enviados).getNumBloco();
                    i = 0;
                    while (this.window + enviados > atual && atual < data.size()) {
                        byte[] packetToSend = s.addSecurityToPacket(data.get(atual).serialize());
                        socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
                        System.out.println("Enviado pacote com o número " + data.get(atual).getNumBloco());
                        this.myWriter.append("Enviado pacote com o número " + data.get(atual).getNumBloco() + "\n");
                        atual++;
                        numB++;
                    }

                    if (this.window > data.size()) {
                        FINPacket fin = new FINPacket((byte) 1);
                        byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
                        DatagramPacket finPacket = (new DatagramPacket(packetToSend,packetToSend.length,ip,port));
                        socket.send(finPacket);
                    }

                    byte[] indata = new byte[1320];
                    DatagramPacket inPacket = new DatagramPacket(indata,1320);
                    socket.receive(inPacket);

                    boolean authenticity = s.verifyPacketAuthenticity(inPacket.getData());

                    if (authenticity) {
                        byte[] packet = inPacket.getData();
                        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));

                        int opcode = bis.read();
                        if (opcode == 6) {
                            ACKPacket ack = ACKPacket.deserialize(bis);
                            // Verificar que o ACK corresponde ao Pacote que enviamos
                            System.out.println("Recebido ACK com o número :" + ack.getNumBloco());
                            this.myWriter.append("Recebido ACK com o número :" + ack.getNumBloco() + "\n");
                            System.out.println("A espera do bloco: " + numB);
                            this.myWriter.append("A espera do bloco: " + numB + "\n");
                            if (ack.getNumBloco() >= data.get(0).getNumBloco()) {
                                if (ack.getNumBloco() == data.get(data.size()-1).getNumBloco() + 1) {
                                    window = data.size()+1;
                                    verificado = true;
                                    enviados += window;
                                    i = 5;
                                }
                                else if (ack.getNumBloco() == numB) {
                                    i = 5;
                                    verificado = true;
                                    enviados += window;
                                    this.window++;
                                }
                                else {
                                    enviados = ack.getNumBloco() - data.get(0).getNumBloco();
                                    window = 1;
                                    numB = data.get(enviados).getNumBloco();
                                }
                            }
                        }
                    }
                }
            }
            catch (SocketTimeoutException e) {
                i++;
            }
        }
    }

}
