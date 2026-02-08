package classes;

public class Major {
    
    private String id;
    private String name;

    public Major(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getMajorID(){
        return id;
    }

    public String getMajorName(){
        return name;
    }

    public void setMajorName(String name){
        this.name = name;
    }
}
