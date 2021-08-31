try {
//NOTE SOMETIMES BIG BIG BIGG on Spigot(Like Descroption) PLUGINS DOESNT WORK WICH CAUSES A ERROR NOT FOUND PLUGIN
//like PermissionsEx
   Resource r = new Resource("smart2uth");
   Author a = r.getAuthor();
   Rating ra = r.getRating();
   System.out.println("Name: "+r.getResourceName()+"\nTested Versions: "+
         r.getTestedVersions().toString()+"\nDownload Link: "+
         r.getDownloadLink()+"\nResource Link: "+r.getResourceLink()+
         "\nDownloads: "+r.getDownloads() + "\nLikes: "+r.getLikes()+"\nAuthor: "
         + a.getName() + "\nAuthor Id: " + a.getId()+"\nTag: "+r.getTag() +
         "\nRatings: "+ra.getAverage() + " " + ra.getCount()+ "\nisPremium: "
         +r.isPermium() +"\nPrice: "+r.getPrice());
   Resource getByid = new Resource(1);//getting resource by id
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Resource Not Found!");
        }
