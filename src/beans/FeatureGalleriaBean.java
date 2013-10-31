package beans;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;


@ManagedBean
@ViewScoped
public class FeatureGalleriaBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int imageCount = 1;
	private List<String> images;

	@PostConstruct
	public void init() {
        images = new ArrayList<String>();  
        
        for(int i = 1; i <= imageCount; i++) {  
            images.add("feature" + i + ".jpg");  
        }  
	}
	
	public List<String> getImages() {
		return images;
	}
}

