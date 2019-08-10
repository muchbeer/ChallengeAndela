package muchbeer.raum.com.challengeandela.utility;

import java.io.Serializable;

public class CarDeals implements Serializable {

    private String id;
    private String carName;
    private String description;
    private String price;
    private String imageUrl;
    private String imageName;

    public CarDeals(){}

    public CarDeals(String carName, String description, String price, String imageUrl,
                    String imageName ) {
        this.setId(id);
        this.setCarName(carName);
        this.setDescription(description);
        this.setPrice(price);
        this.setImageUrl(imageUrl);
        this.setImageName(imageName);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String title) {
        this.carName = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

}
