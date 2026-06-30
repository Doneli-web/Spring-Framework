package framework.route;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String methode;

    public UrlMethod(String url, String methode) {
        this.url = url;
        this.methode = methode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UrlMethod urlMethod = (UrlMethod) object;
        return Objects.equals(url, urlMethod.url) && Objects.equals(methode, urlMethod.methode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methode);
    }
}
