package group.tonight.roommigrationdemo.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity//如果未指定表名，则以类名作为表名
public class Word implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String word;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}