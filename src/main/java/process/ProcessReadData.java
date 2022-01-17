package process;

import java.util.HashMap;
import java.util.Map;

import read.ReadLocalFileServiceImpl;
import read.ReadPOJO;
import read.ReadService;
import read.ReadURLFileServiceImpl;
import validators.implementations.ReadPOJOValidatorImpl;
import validators.injectors.ValidatorServiceInjector;
import validators.injectors.URLValidatorServiceInjector;
import validators.services.ValidatorService;
import write.WriteObj;

public class ProcessReadData implements ProcessData{
	
	private ReadService rs;
	private Map<String, WriteObj> results = new HashMap<>();
	
	@Override
	public void process(String dataSource) {
		ValidatorServiceInjector<String> injector = 
				new URLValidatorServiceInjector();
		ValidatorService<String> validator = injector.getValidator();
		if(validator.isValid(dataSource)) {
			rs = new ReadURLFileServiceImpl();
		} else {
			rs = new ReadLocalFileServiceImpl();
		}
	}
	
	@Override
	public void execute() {
		ReadPOJO rPojo;
		ValidatorService<ReadPOJO> readPOJOValidator = new ReadPOJOValidatorImpl();
		while(rs.hasNext()) {
			rPojo = (ReadPOJO) rs.next(ReadPOJO.class);
			if(readPOJOValidator.isValid(rPojo)) {
				results.put(rPojo.getPath(), new WriteObj(rPojo.getUrl(), rPojo.getSize()));
			}
		}
	}
	
	public Map<String, WriteObj> getResults(){
		return results;
	}

	@Override
	public Boolean call() throws Exception {
		return true;
	}
}
