import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerWorker implements Runnable{
    public DatagramPacket received;
    public DatagramSocket socket;
    public File folder;
    public InetAddress[] ips;
    public Security s;

    public ServerWorker(DatagramPacket received,File folder,InetAddress[] ips) {
        this.received = received;
        this.folder = folder;
        this.ips = ips;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            socket.setSoTimeout(1000);
            int i = 0;
            // Vemos a informação do cliente no Packet.
            int port = this.received.getPort();
            InetAddress clientIP = this.received.getAddress();
            while (i < 25){
                try {
                    DatagramPacket dp = this.received;

                    Security s = new Security();
                    boolean authenticity = s.verifyPacketAuthenticity(dp.getData());

                    if (authenticity) {
                        byte[] packet = dp.getData();
                        i = 25;    
                        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(packet,20,packet.length));
                        
                        // Lemos o opcode que veio no Packet.
                        int opcode = bis.read();
                        System.out.println(opcode);

                        // Se opcode == 1 , enviamos a informaçao dos ficheiros para o cliente e no fim,enviamos um FINPacket.
                        if (opcode == 1) {
                            sendFileInfo(clientIP,port);
                            FINPacket fin = new FINPacket();
                            byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
                            DatagramPacket outPacket = (new DatagramPacket(packetToSend,packetToSend.length,clientIP,port));
                            socket.send(outPacket);
                        }
                        // Se opcode == 2, recebemos um pedido de leitura de um ficheiro. Enviamos o ficheiro ao cliente e no fim enviamos um FINPacket.
                        if (opcode == 2) {
                            ReadFilePacket readFile = ReadFilePacket.deserialize(bis);
                            sendFile(readFile,clientIP,port);
                            FINPacket fin = new FINPacket();
                            byte[] packetToSend = s.addSecurityToPacket(fin.serialize());
                            DatagramPacket outPacket = (new DatagramPacket(packetToSend,packetToSend.length,clientIP,port));
                            socket.send(outPacket);
                        }
                        //Se opcode == 5, recebemos um FINPacket. Isso significa que já enviamos um FINPacket e assim saímos.
                        if (opcode == 5) {
                            i = 25;
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
        for (File f  : fSend) {
            String filename = f.getAbsolutePath();
            //Criar o path relativo para o ficheiro.
            Path file = Path.of(filename);
            Path parent = folder.toPath();
            file = parent.relativize(file);
            FileInfo fi = new FileInfo(file.toString(),Long.toString(f.lastModified()));
            // Se a informação do ficheiro já nao tiver espaço no pacote, entao enviamos o pacote e começamos um novo onde escrevemos a informaçao do ficheiro.
            if ((bos.size() + fi.serialize().length + 1) > 1292) {
                bos.write(0);
                byte[] data = bos.toByteArray();
                DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
                sendDataPacket(fileInfos, ip, port);
                bos.close();
                bos = new ByteArrayOutputStream();
            }
            bos.write('+');
            bos.write(fi.serialize());
        }
        // O byte 0 indica que não temos mais dados;
        bos.write(0);
        byte[] data = bos.toByteArray();
        DataTransferPacket fileInfos = new DataTransferPacket(numB++, data.length, data);
        sendDataPacket(fileInfos, ip, port);
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
        Path file = Path.of(folder.getAbsolutePath()).resolve(f);
        System.out.println("A enviar o ficheiro " + file.toString());
        // Verificar que estao a pedir um ficheiro existente.
        File ficheiro = new File(file.toString());
        if (!ficheiro.exists()) {
            System.out.println("Ficheiro nao existe");
            return;
        }
        FileInputStream fis = new FileInputStream(ficheiro);
        int numB = 1;
        // Enquanto houver bytes para ler, enviamos os dados num DataTransferPacket.
        while (fis.available() != 0) {
            byte[] fileData = fis.readNBytes(1293);
            DataTransferPacket dtFile = new DataTransferPacket(numB++, fileData.length, fileData);
            sendDataPacket(dtFile, clientIP, port);
        }
        System.out.println("Ficheiro acabado de enviar");
        fis.close();
    }

    // Envia um DataTransferPacket para o ip e port designados.
    public void sendDataPacket (DataTransferPacket data,InetAddress ip, int port) throws IOException{
        boolean verificado = false;
        int i = 0;
        socket.setSoTimeout(1000);
        while (i < 5) {
            try {
                // Enquando nao se verificar que o cliente recebeu o pacote, enviamos o pacote.
                while (!verificado) {
                    byte[] packetToSend = s.addSecurityToPacket(data.serialize());
                    socket.send(new DatagramPacket(packetToSend, packetToSend.length,ip,port));
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
                            if (ack.getNumBloco() == data.getNumBloco()) {
                                verificado = true;
                                i = 5;
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
