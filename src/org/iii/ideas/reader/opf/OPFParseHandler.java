package org.iii.ideas.reader.opf;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * parse opf xml的sax handler
 * @author III
 * 
 */
public class OPFParseHandler extends DefaultHandler{
	//opf parser
	private boolean in_title=false;	
	private boolean in_creator=false;
	private boolean in_date=false;
	private boolean in_subject=false;
	private boolean in_language=false;
	private boolean in_coverage=false;
	private boolean in_rights=false;
	private boolean in_publisher=false;
	private boolean in_identifier=false;
	private boolean in_description=false;
	private boolean in_type=false;
	private boolean in_format=false;
	private boolean in_source=false;
	private boolean in_relation=false;
	//private boolean in_manifest=false;
	private boolean in_metadata=false;
	//private boolean in_spine=false;
	private String id, href, idref;
	
	/**
	 * opf metadata tag
	 * @author III
	 *
	 */
	public static enum opfTag {title,creator,date,subject,
		language,
		coverage,
		rights,
		publisher,
		identifier,
		description,
		type,
		format,
		source,
		relation,
		manifest,
		spine,
		item,
		itemref,
		metadata};

	//初始化data set
	private OPFDataSet dataset=new OPFDataSet();

	/**
	 * 取得parse完畢的metadata
	 * @return opf data set
	 */
	public  OPFDataSet getDataSet(){
		return this.dataset;
	}
	
	public void startDocument() throws SAXException {
        //dataset = new OPFDataSet();
   }
	public void endDocument() throws SAXException {
        // do nothing
   }  
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		try {
			//Log.d("OPFdebug",localName);
			switch (opfTag.valueOf(localName)) {
			case title:
	            in_title = true;
				break;
			case creator:
	            in_creator = true;
				break;
			case date:
	            in_date = true;
				break;
			case subject:
	    	    in_subject=true;
				break;
			case language:
	    	    in_language=true;
				break;
			case coverage:
	    	    in_coverage=true;
				break;
			case rights:
	    	    in_rights=true;
				break;
			case publisher:
	    	    in_publisher=true;
				break;
			case identifier:
	    	    in_identifier=true;
				break;
			case description:
	    	    in_description=true;
				break;
			case type:
	    	    in_type=true;
				break;
			case format:
	    	    in_format=true;
				break;
			case source:
	    	    in_source=true;
				break;
			case relation:
				in_relation=true;
				break;
			case manifest:
				//in_manifest=true;
				break;
			case spine:
				//in_spine=true;
				break;
			case item:
				id=atts.getValue("id");
				href=atts.getValue("href");
				dataset.addItems(id,href);
				break;
			case itemref:
				idref=atts.getValue("idref");
				dataset.addSpine(idref);
				break;
			case metadata:
				in_metadata=true;
				break;
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
  } 
	
	public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
		//Log.d("OPFdebug",localName); 
		try {
			//Log.d("OPFdebug",localName);
			switch (opfTag.valueOf(localName)) {
			case title:
	            in_title = false;
				break;
			case creator:
	            in_creator = false;
				break;
			case date:
	            in_date = false;
				break;
			case subject:
	    	    in_subject=false;
				break;
			case language:
	    	    in_language=false;
				break;
			case coverage:
	    	    in_coverage=false;
				break;
			case rights:
	    	    in_rights=false;
				break;
			case publisher:
	    	    in_publisher=false;
				break;
			case identifier:
	    	    in_identifier=false;
				break;
			case description:
	    	    in_description=false;
				break;
			case type:
	    	    in_type=false;
				break;
			case format:
	    	    in_format=false;
				break;
			case source:
	    	    in_source=false;
				break;
			case relation:
				in_relation=false;
				break;
			case manifest:
				//in_manifest=false;
				break;
			case spine:
				//in_spine=false;
				break;
			case item:
				break;
			case itemref:
				break;
			case metadata:
				in_metadata=false;
				break;
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		/*if (localName.equals("title")) {
            in_title = false;
       }else if (localName.equals("creator")) {
            in_creator = false;
       }else if (localName.equals("date")) {
            in_date = false;
       }else if (localName.equals("subject")) {
    	    in_subject=false;
       }else if (localName.equals("language")) {
    	    in_language=false;
       }else if (localName.equals("coverage")) {
    	    in_coverage=false;
       }else if (localName.equals("rights")) {
    	    in_rights=false;
       }else if (localName.equals("publisher")) {
    	    in_publisher=false;
       }else if (localName.equals("identifier")) {
    	    in_identifier=false;
       }else if (localName.equals("description")) {
    	    in_description=false;
       }else if (localName.equals("type")) {
    	    in_type=false;
       }else if (localName.equals("format")) {
    	    in_format=false;
       }else if (localName.equals("source")) {
    	    in_source=false;
       }else if (localName.equals("relation")) {
    	    in_relation=false;
       }else if (localName.equals("manifest")) {
    	    in_manifest=false;
       }else if (localName.equals("spine")) {
    	    in_spine=false;
       }	*/
  }
	
    public void characters(char ch[], int start, int length) {
    	//Log.d("OPFdebug","t:"+in_title+"c:"+in_creator);
        if(!in_metadata){
        	
        }else if(this.in_title){
        	dataset.setTitle(new String(ch, start, length));
        }
        else if(this.in_coverage){
            dataset.setCoverage(new String(ch, start, length));
        }
        else if(this.in_creator){
            dataset.addCreator(new String(ch, start, length));
        }
        else if(this.in_date){
            dataset.setDate(new String(ch, start, length));
        }
        else if(this.in_description){
            dataset.setDescription(new String(ch, start, length));
        }
        else if(this.in_format){
            dataset.setFormat(new String(ch, start, length));
        }
        else if(this.in_identifier){
            dataset.setIdentifier(new String(ch, start, length));
        }
        else if(this.in_language){
            dataset.setLanguage(new String(ch, start, length));
        }
        else if(this.in_publisher){
            dataset.setPublisher(new String(ch, start, length));
        }
        else if(this.in_relation){
            dataset.setRelation(new String(ch, start, length));
        }
        else if(this.in_rights){
            dataset.setRights(new String(ch, start, length));
        }
        else if(this.in_source){
            dataset.setSource(new String(ch, start, length));
        }
        else if(this.in_subject){
            dataset.setSubject(new String(ch, start, length));
        }
        else if(this.in_type){
            dataset.setType(new String(ch, start, length));
        }
  }
}
