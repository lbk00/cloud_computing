package aws;

/*
* Cloud Computing
* 
* Dynamic Resource Management Tool
* using AWS Java SDK Library
* 
*/
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.amazonaws.SdkClientException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;

public class awsTest {

	static AmazonEC2      ec2;
	
	private static final String PRIVATE_KEY = "/home/user/cloud-test.pem";  //EC2 인스턴스의 .pem 파일 경로 ( private_key )
	
	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("ap-southeast-2")	/* check the region at AWS console */
			.build();
	}
	
	// EC2 요금표 (샘플 요금, 실제 요금은 AWS 공식 문서를 참조)
    	private static final Map<String, Double> instanceHourlyRates = new HashMap<String, Double>() {{
		put("t2.micro", 0.0116); // USD/hour
		put("t2.small", 0.023);  // USD/hour
		put("t2.medium", 0.0464);
		// 필요한 인스턴스 유형과 요금을 추가
    	}};
	
	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones        ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("  9. condor_status               99. quit                   ");
			//System.out.println("  9. condor_status               10. name_change           ");
			//System.out.println(" 11. costAnalyze                 99. quit                   ");
			System.out.println("------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration!");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				
			case 3: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					startInstance(instance_id);
				break;

			case 4: 
				availableRegions();
				break;

			case 5: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					stopInstance(instance_id);
				break;

			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(id_string.hasNext())
					ami_id = id_string.nextLine();
				
				if(!ami_id.trim().isEmpty()) 
					createInstance(ami_id);
				break;

			case 7: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					rebootInstance(instance_id);
				break;

			case 8: 
				listImages();
				break;
			case 9: 
				condorStatus();
				break;	
				
			case 10: 
				nameChange();
				break;
				
			case 11: 
				costAnalyze();
				break;
				
			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;
			default: System.out.println("concentration!");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
				/*
				// 태그에서 Name 찾기
					String instanceName = null;
					for (Tag tag : instance.getTags()) {
					    if ("Name".equals(tag.getKey())) {
						instanceName = tag.getValue();
						break;
					    }
					}

					// 이름 출력
					if (instanceName != null) {
					    System.out.printf(", [name] %s ", instanceName);
					} else {
					    System.out.print(", [name] <No Name>");
					}
					*/
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
				System.out.println("Caught Exception: " + ase.getMessage());
				System.out.println("Reponse Status Code: " + ase.getStatusCode());
				System.out.println("Error Code: " + ase.getErrorCode());
				System.out.println("Request ID: " + ase.getRequestId());
		}
	
	}

	public static void startInstance(String instance_id)
	{
		
		System.out.printf("Starting .... %s\n", instance_id);
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
			() -> {
			StartInstancesRequest request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		StartInstancesRequest request = new StartInstancesRequest()
			.withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}
	
	
	public static void availableRegions() {
		
		System.out.println("Available regions ....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	
	public static void stopInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
			() -> {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);
	
			ec2.stopInstances(request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}
	
	public static void createInstance(String ami_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		RunInstancesRequest run_request = new RunInstancesRequest()
			.withImageId(ami_id)
			.withInstanceType(InstanceType.T2Micro)
			.withMaxCount(1)
			.withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
			"Successfully started EC2 instance %s based on AMI %s",
			reservation_id, ami_id);
	
	}

	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = ec2.rebootInstances(request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

		
	}
	
	public static void listImages() {
		System.out.println("Listing images....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		DescribeImagesRequest request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		
		request.getFilters().add(new Filter().withName("owner-id").withValues("676206900017"));
		request.setRequestCredentialsProvider(credentialsProvider);
		
		DescribeImagesResult results = ec2.describeImages(request);
		
		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n", 
					images.getImageId(), images.getName(), images.getOwnerId());
		}
		
	}
	
	
	// condor_status를 실행할 인스턴스의 id를 입력받고, 해당 인스턴스에서 실행
	public static void condorStatus() {
        	try {
        		
        	    // 사용자로부터 인스턴스 ID 입력받기
		    Scanner scanner = new Scanner(System.in);
		    System.out.print("Enter instance id: ");
		    String id = scanner.nextLine();
		    //DNS 
		    String HOST = get_DNS(id);
		    
		    // ssh 명령어를 사용하여 EC2에 접속
		    // ssh -i "cloud-test.pem" ec2-user@ec2-3-25-120-116.ap-southeast-2.compute.amazonaws.com 형식
		    String command = String.format("ssh -i %s ec2-user@%s condor_status", PRIVATE_KEY, HOST);
		    
		    // ProcessBuilder를 통해 ssh 명령어 실행
		    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
		    processBuilder.redirectErrorStream(true);
		    Process process = processBuilder.start();

		    // 명령어 실행 결과를 읽기
		    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    StringBuilder result = new StringBuilder();
		    String result_line;
		    while ((result_line = reader.readLine()) != null) {
		        result.append(result_line).append("\n");
		    }
		    // 프로세스 종료 후 상태 확인
		    int exitCode = process.waitFor();
		    if (exitCode == 0) {
		        System.out.println("--- condor_status 수행 결과 ---");
		        System.out.println(result.toString());
		    } else {
		        System.err.println("condor_status failed " + exitCode);
		    }

		} catch (Exception e) {
		    System.err.println("Error : " + e.getMessage());
		    e.printStackTrace();
		}
    	}
    	// DNS 반환
    	private static String get_DNS(String id) {
		try {
		    DescribeInstancesRequest request = new DescribeInstancesRequest()
		        .withInstanceIds(id);
		    DescribeInstancesResult result = ec2.describeInstances(request);
		    for (Reservation reservation : result.getReservations()) {
		        for (Instance instance : reservation.getInstances()) {
		            return instance.getPublicDnsName(); // ssh 접속에 필요한 퍼블릭 DNS 반환
		        }
		    }
		} catch (Exception e) {
		    System.err.println("Error : " + e.getMessage());
		    e.printStackTrace();
		}
		return null;
   	 }	
    	
	// 간단한 비용 분석 기능
	public static void costAnalyze() {
        System.out.println("Analyzing EC2 instance costs...");

        try {
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            DescribeInstancesResult result = ec2.describeInstances(request);

            double totalCost = 0.0;
            int totalInstances = 0;

            for (Reservation reservation : result.getReservations()) {
                List<Instance> instances = reservation.getInstances();
                for (Instance instance : instances) {
                    String instanceId = instance.getInstanceId();
                    String instanceType = instance.getInstanceType();
                    String state = instance.getState().getName();

                    if (!state.equals("running")) {
                        continue; // 실행 중인 인스턴스만 계산
                    }

                    totalInstances++;
                    double hourlyRate = instanceHourlyRates.getOrDefault(instanceType, 0.0);
                    totalCost += hourlyRate;

                    System.out.printf("Instance ID: %s, Type: %s, Hourly Rate: $%.4f\n",
                            instanceId, instanceType, hourlyRate);
                }
            }

            System.out.printf("Total running instances: %d\n", totalInstances);
            System.out.printf("Estimated hourly cost: $%.4f\n", totalCost);

        } catch (AmazonServiceException e) {
            System.err.println("AWS service error: " + e.getMessage());
        } catch (SdkClientException e) {
            System.err.println("AWS SDK client error: " + e.getMessage());
        }
    }
    
    
    private static void nameChange() {
        try {
        	
            // 사용자로부터 인스턴스 ID 입력
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Enter instance id: ");
	    String instanceId = scanner.nextLine();
            
            // 사용자로부터 인스턴스 name 입력
	    System.out.print("Enter name to change : ");
	    String newName = scanner.nextLine();
            // 태그 생성
            Tag nameTag = new Tag()
                .withKey("Name")
                .withValue(newName);

            // 태그를 인스턴스에 적용
            CreateTagsRequest request = new CreateTagsRequest()
                .withResources(instanceId) // 인스턴스 ID를 지정
                .withTags(nameTag);

            ec2.createTags(request);
            System.out.println("The instance " + instanceId + " has been renamed to: " + newName);

        } catch (Exception e) {
            System.err.println("Failed to rename the instance: " + e.getMessage());
            e.printStackTrace();
        }
    }
	
}
	
