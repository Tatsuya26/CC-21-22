import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ArmazemFicheiro {
    private Lock lock;
    private Map<String,FileInfo> ficheiros;

    public ArmazemFicheiro() {
        this.lock = new ReentrantLock();
        this.ficheiros = new HashMap<>();
    }

    public ArmazemFicheiro(File diretoria) {
        this.lock = new ReentrantLock();
        this.ficheiros = new HashMap<>();
        List<File> ficheirosSistema = new ArrayList<>();
        adicionarFicheirosSistema(diretoria,ficheirosSistema);
        for (File f : ficheirosSistema) {
            String filename = f.getAbsolutePath();
            Path file = Path.of(filename);
            Path parent = Path.of(diretoria.getAbsolutePath());
            file = parent.relativize(file);
            FileInfo fi = new FileInfo(file.toString(),Long.toString(f.lastModified()));
            adicionaFileInfo(fi);
        }
    }

    private void adicionarFicheirosSistema(File folder,List<File> listFiles) {
        File[] subFicheiros = folder.listFiles();
        for (File f  : subFicheiros) {
            if (!f.isHidden()) {
                if (f.isDirectory())
                    adicionarFicheirosSistema(f,listFiles);
                else {
                    listFiles.add(f);
                }
            }
        }
    }

    public void adicionaFileInfo (FileInfo fi) {
        try{
            lock.lock();
            if (this.ficheiros.containsKey(fi.getName())) {
                FileInfo f = this.ficheiros.get(fi.getName());
                if (fi.compareTo(f) > 0) this.ficheiros.replace(fi.getName(),fi);
            }
            else this.ficheiros.put(fi.getName(),fi);
        }
        finally {
            lock.unlock();
        }
    }

    public List<FileInfo> getList() {
        return this.ficheiros.values().stream().collect(Collectors.toList());
    }

    public boolean isSincronizado() {
        for (FileInfo f : this.ficheiros.values()) {
            if (f.getIP() != null) return false;
        }
        return true;
    }


}
