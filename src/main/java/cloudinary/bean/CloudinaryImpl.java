package cloudinary.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.SingletonManager;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryImpl {
	
	@Autowired
    private MessageSource messageSource;
	
	public Cloudinary cloudinary;
	
	public void initialize() {
		cloudinary = new Cloudinary(ObjectUtils.asMap(
				  "cloud_name", messageSource.getMessage("system.cloud.name",null,null),
				  "api_key", messageSource.getMessage("system.api.key",null,null),
				  "api_secret", messageSource.getMessage("system.api.secret",null,null)));
        
        SingletonManager manager = new SingletonManager();
	  		manager.setCloudinary(cloudinary);
	  		manager.init();
	}

}
