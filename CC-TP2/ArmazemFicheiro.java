import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
        File[] subFicheiros = diretoria.listFiles();
        try {
            for (File f  : subFicheiros) {
                BasicFileAttributes fa = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                String filename = f.getAbsolutePath();
                Path file = Path.of(filename);
                Path parent = file.getParent().getParent();
                file = parent.relativize(file);
                FileInfo fi = new FileInfo(file.toString(),Long.toString(fa.lastModifiedTime().toMillis()));
                adicionaFileInfo(fi);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
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

}
