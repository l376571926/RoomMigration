package group.tonight.roommigrationdemo.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Pig implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int pigId;
    private String name;

    public int getPigId() {
        return pigId;
    }

    public void setPigId(int pigId) {
        this.pigId = pigId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
