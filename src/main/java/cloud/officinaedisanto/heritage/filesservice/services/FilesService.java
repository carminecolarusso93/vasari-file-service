package cloud.officinaedisanto.heritage.filesservice.services;

import cloud.officinaedisanto.heritage.filesservice.model.File;
import com.blazebit.persistence.CriteriaBuilderFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.OutputStream;

@ApplicationScoped
@Transactional
public class FilesService {

    private final static String OBJECT_CLASS = "cloud.officinaedisanto.heritage.filesservice.model.File";

    private final EntityManager em;
    private final CriteriaBuilderFactory cbf;
    private final S3Client s3Client;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    public FilesService(EntityManager em, CriteriaBuilderFactory cbf, S3Client s3Client) {
        this.em = em;
        this.cbf = cbf;
        this.s3Client = s3Client;
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
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getUuid().toString())
                .build();
        var getObjectResponse =
                s3Client.getObject(getObjectRequest, ResponseTransformer.toOutputStream(outputStream));
        if (!getObjectResponse.sdkHttpResponse().isSuccessful()) {
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
        var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(type)
                .build();
        var putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
        if (!putObjectResponse.sdkHttpResponse().isSuccessful()) {
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
        var deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getUuid().toString())
                .build();
        var deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);
        if (!deleteObjectResponse.sdkHttpResponse().isSuccessful()) {
            throw new InternalServerErrorException();
        }
        em.remove(file);
    }

}
