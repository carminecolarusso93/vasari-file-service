package cloud.officinaedisanto.heritage.filesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class File extends BaseEntity {

    @JsonIgnore
    private UUID uuid = UUID.randomUUID();

    private String name;
    private String type;

    public File() {
    }

    public File(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
