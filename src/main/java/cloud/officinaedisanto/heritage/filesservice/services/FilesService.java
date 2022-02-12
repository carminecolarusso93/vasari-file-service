package cloud.officinaedisanto.heritage.filesservice.services;

import cloud.officinaedisanto.heritage.filesservice.model.File;
import com.blazebit.persistence.CriteriaBuilderFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@ApplicationScoped
@Transactional
public class FilesService {

    private final EntityManager em;
    private final CriteriaBuilderFactory cbf;

    @ConfigProperty(name = "storage.path")
    String storagePath;

    public FilesService(EntityManager em, CriteriaBuilderFactory cbf) {
        this.em = em;
        this.cbf = cbf;
    }

    //Support methods

    public File getFile(long id) {
        var criteriaBuilder = cbf.create(em, File.class)
                .where("id").eq(id);
        if (criteriaBuilder.getResultList().isEmpty()) {
            throw new NotFoundException();
        }
        return criteriaBuilder.getSingleResult();
    }

    //Backoffice methods

    public void get(long id, OutputStream outputStream) {
        var file = getFile(id);
        var filePath = Paths.get(storagePath, file.getUuid().toString());
        try {
            Files.copy(filePath, outputStream);
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    public File save(String name, String type, byte[] fileBytes) {
        var file = new File(name, type);
        putObject(file.getUuid().toString(), type, fileBytes);
        em.persist(file);
        return file;
    }

    private void putObject(String key, String type, byte[] fileBytes) {
        var filePath = Paths.get(storagePath, key);
        try {
            Files.write(filePath, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    public File update(long id, String name, String type, byte[] fileBytes) {
        var file = getFile(id);
        putObject(file.getUuid().toString(), type, fileBytes);
        file.setName(name);
        file.setType(type);
        em.persist(file);
        return file;
    }

    public void delete(long id) {
        var file = getFile(id);
        var filePath = Paths.get(storagePath, file.getUuid().toString());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
        em.remove(file);
    }

}
