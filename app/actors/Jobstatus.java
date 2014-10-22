package actors;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by Ott Konstantin on 21.10.2014.
 */
public class Jobstatus  {
    private String type;
    private int worker;
    private int count;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWorker() {
        return worker;
    }

    public void setWorker(int worker) {
        this.worker = worker;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCamelType() {
        return WordUtils.capitalize(type.toLowerCase());
    }
}
