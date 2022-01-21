package cloud.officinaedisanto.heritage.filesservice.api.backoffice;

import cloud.officinaedisanto.heritage.filesservice.model.File;
import cloud.officinaedisanto.heritage.filesservice.model.FormData;
import cloud.officinaedisanto.heritage.filesservice.services.FilesService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Tag(name = "files-backoffice")
@Path(("/api/v1/bo/files"))
@Authenticated
public class FilesBOResource {

    private final FilesService filesService;

    public FilesBOResource(FilesService filesService) {
        this.filesService = filesService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response get(@PathParam("id") long id) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        var file = filesService.getFile(id);
        filesService.get(id, outputStream);
        Response.ResponseBuilder response = Response.ok((StreamingOutput) outputStream::writeTo);
        response.header("Access-Control-Expose-Headers", "Content-Disposition");
        if (file.getName() != null && !file.getName().isBlank()) {
            response.header("Content-Disposition", "attachment;filename=" + file.getName());
        }
        if (file.getType() != null && !file.getType().isBlank()) {
            response.header("Content-Type", file.getType());
        }
        return response.build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public File save(@MultipartForm FormData formData) {
        try {
            return filesService.save(formData.getFileName(), formData.getMimeType(), formData.getData().readAllBytes());
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public File update(@PathParam("id") long id, @MultipartForm FormData formData) {
        try {
            return filesService.update(id, formData.getFileName(), formData.getMimeType(), formData.getData().readAllBytes());
        } catch (IOException e) {
            throw new InternalServerErrorException();
        }
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        filesService.delete(id);
    }

}
