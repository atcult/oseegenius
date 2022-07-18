/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package org.jzkit.z3950.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jzkit.ServiceDirectory.SearchServiceDescriptionDBO;
import org.jzkit.a2j.codec.util.OIDRegister;
import org.jzkit.a2j.codec.util.OIDRegisterEntry;
import org.jzkit.a2j.gen.AsnUseful.EXTERNAL_type;
import org.jzkit.a2j.gen.AsnUseful.encoding_inline0_type;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.search.provider.iface.ScanInformation;
import org.jzkit.search.provider.iface.ScanRequestInfo;
import org.jzkit.search.provider.iface.Scanable;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.util.QueryModel.Internal.AttrPlusTermNode;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.jzkit.search.util.RecordBuilder.RecordBuilder;
import org.jzkit.search.util.RecordModel.ArchetypeRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.InformationFragment;
import org.jzkit.z3950.gen.v3.NegotiationRecordDefinition_charSetandLanguageNegotiation_3.CharSetandLanguageNegotiation_type;
import org.jzkit.z3950.gen.v3.NegotiationRecordDefinition_charSetandLanguageNegotiation_3.Iso10646_type;
import org.jzkit.z3950.gen.v3.NegotiationRecordDefinition_charSetandLanguageNegotiation_3.OriginProposal_type;
import org.jzkit.z3950.gen.v3.NegotiationRecordDefinition_charSetandLanguageNegotiation_3.proposedCharSets_inline0_choice1_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.AttributeElement_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.DefaultDiagFormat_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.DeleteResultSetRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.DeleteResultSetResponse_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.DiagRec_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.ElementSetNames_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.InitializeRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.NamePlusRecord_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.OtherInformationItem43_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.PDU_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.PresentRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.PresentResponse_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.Records_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.ScanRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SearchRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SearchResponse_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SortRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SortResponse_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.addinfo_inline14_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.information_inline44_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.recordComposition_inline9_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.record_inline13_type;
import org.jzkit.z3950.util.APDUEvent;
import org.jzkit.z3950.util.GenericEventToTargetListenerAdapter;
import org.jzkit.z3950.util.TargetAPDUListener;
import org.jzkit.z3950.util.Z3950Constants;
import org.jzkit.z3950.util.ZTargetEndpoint;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;

import com.atc.osee.z3950.IConstants;
import com.atc.osee.z3950.backend.BackendSortDTO;
import com.atc.osee.z3950.backend.OseeGeniusNonBlockingBackend;

public class ZServerAssociation implements TargetAPDUListener, ApplicationContextAware 
{
	public static final Log LOGGER = LogFactory.getLog(ZServerAssociation.class);
	public static final Log EVENT_LOGGER = LogFactory.getLog(Z3950Main.class);
	
	private ZTargetEndpoint endPoint;
	private OIDRegister registry;
	private GenericEventToTargetListenerAdapter eventAdapter;
	private OseeGeniusNonBlockingBackend backend;
	private ApplicationContext applicationContext;
	private SocketAddress clientAddress;

	private Scanable scanService;
	private SearchServiceDescriptionDBO browseServiceDefinition;
	
	public ZServerAssociation(final Socket socket, final Z3950NonBlockingBackend backend, final ApplicationContext applicationContext) 
	{
		// Force downcasting in order to support SORT operation.
		this.backend = (OseeGeniusNonBlockingBackend)backend;
		registry = (OIDRegister) applicationContext.getBean("OIDRegister");
		endPoint = new ZTargetEndpoint(socket, registry);
		eventAdapter = new GenericEventToTargetListenerAdapter(this);
		endPoint.getPDUAnnouncer().addObserver(eventAdapter);

		clientAddress = socket.getRemoteSocketAddress();

		if (registry == null)
		{
			throw new RuntimeException("Unable to locate OID Register in Application Context");
		}
		
		this.applicationContext = applicationContext;
		endPoint.start();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void incomingInitRequest(final APDUEvent event) 
	{		
		LOGGER.debug("INIT request received.");
		InitializeRequest_type initRequest = (InitializeRequest_type) (event.getPDU().o);
		
		for (int i = 0; i < Z3950Constants.z3950_option_names.length; i++) 
		{
			if (initRequest.options.isSet(i))
			{
				LOGGER.debug("Origin requested service: "+ Z3950Constants.z3950_option_names[i]);
			}
		}

		// Capabilities exchange: clear unsupported operation
		initRequest.options.clearBit(3);
		initRequest.options.clearBit(4);
		initRequest.options.clearBit(5);
		initRequest.options.clearBit(6);
		initRequest.options.clearBit(9);
		initRequest.options.clearBit(10);
		initRequest.options.clearBit(11);
		initRequest.options.clearBit(12);
		initRequest.options.clearBit(13);
		initRequest.options.clearBit(14);
		initRequest.options.clearBit(15);
		initRequest.options.clearBit(16);
		initRequest.options.clearBit(17);
		initRequest.options.clearBit(18);
		initRequest.options.clearBit(19);
		
		// SCAN
		if ( initRequest.options.isSet(7) )
		{
			Configuration configuration = (Configuration) applicationContext.getBean(IConstants.JZKIT_CONFIG_BEAN_NAME);
			try 
			{
				browseServiceDefinition = configuration.lookupSearchService(IConstants.SCAN_SERVICE__NAME);
				if (browseServiceDefinition != null)
				{
					Searchable scanSearchableService = browseServiceDefinition.newSearchable();
					scanSearchableService.setApplicationContext(applicationContext);
					scanService = (Scanable)scanSearchableService;
				} else 
				{
					initRequest.options.clearBit(7);
					LOGGER.debug("Origin requested SCAN, but this is not supported by this instance.");					
				}
			} catch (Exception exception)
			{
				initRequest.options.clearBit(7);
				LOGGER.error("Error while looking up SCAN service provider.", exception);
			} 
		 }

		// If we are talking v2, userInformationField may contain a character
		// Set Negotiation
		// field, if v3, otherInfo may contain a charset/language negotiation
		// feature.
		if (initRequest.userInformationField != null) 
		{
			OIDRegisterEntry ent = registry.lookupByOID(initRequest.userInformationField.direct_reference);
			if (ent != null)
			{
				LOGGER.debug("Init Request contains userInformationField oid="+ ent.getName());
			}
			else
			{
				LOGGER.debug("Unkown external in userInformationField");
			}
		}

		if (initRequest.otherInfo != null) 
		{
			LOGGER.debug("Init Request contains otherInfo entries");
			for (Iterator other_info_enum = initRequest.otherInfo.iterator(); other_info_enum.hasNext();) 
			{
				LOGGER.debug("Processing otherInfo entry...");
				// Process the external at other_info_enum.nextElement();
				OtherInformationItem43_type oit = (OtherInformationItem43_type) (other_info_enum.next());

				LOGGER.debug("Processing OtherInformationItem43_type");

				switch (oit.information.which) 
				{
					case information_inline44_type.externallydefinedinfo_CID:
						EXTERNAL_type et = (EXTERNAL_type) (oit.information.o);
						if (et.direct_reference != null) 
						{
							OIDRegisterEntry ent = registry.lookupByOID(et.direct_reference);
							LOGGER.debug("External with direct reference, oid="+ ent.getName());

							// Are we dealing with character set negotiation.
							if (ent.getName().equals("z_charset_neg_3"))
							{
								handleNLSNegotiation((CharSetandLanguageNegotiation_type) (et.encoding.o));
							}
						}
						break;
					default:
						LOGGER.debug("Detected an unhandled OtherInformationType. Ignoring the packet.");
						break;
					}
				}
			}

			try 
			{
				EVENT_LOGGER.debug("A new Z3950 session has been started. Requestor is " + clientAddress);
				LOGGER.debug("Sending back INIT response.");
		
				endPoint.setCharsetEncoding(ZTargetEndpoint.UTF_8_ENCODING);
				endPoint.sendInitResponse(
						initRequest.referenceId,
						initRequest.protocolVersion, 
						initRequest.options,
						initRequest.preferredMessageSize.longValue(),
						initRequest.exceptionalRecordSize.longValue(), 
						true,
						null, 
						backend.getImplName(),
						backend.getVersion(), 
						null, 
						null);
		} catch (IOException exception) 
		{
			LOGGER.error("Unable to send back the INIT response.", exception);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleNLSNegotiation(CharSetandLanguageNegotiation_type neg) 
	{
		LOGGER.debug("Handle Character Set and Language Negotiation");

		if (neg.which == CharSetandLanguageNegotiation_type.proposal_CID) {
			OriginProposal_type op = (OriginProposal_type) (neg.o);

			// Deal with any proposed character sets.
			if (op.proposedCharSets != null) 
			{
				for (Iterator prop_charsets = op.proposedCharSets.iterator(); prop_charsets.hasNext();) 
				{
					proposedCharSets_inline0_choice1_type c = (proposedCharSets_inline0_choice1_type) (prop_charsets.next());
					switch (c.which) 
					{
						case proposedCharSets_inline0_choice1_type.iso10646_CID:
							// The client proposes an iso 10646 id for a character
							// set
							Iso10646_type iso_type = (Iso10646_type) (c.o);
							OIDRegisterEntry ent = registry.lookupByOID(iso_type.encodingLevel);
							LOGGER.debug("Client proposes iso10646 charset: " + ent.getName());
							break;
						default:
							LOGGER.error("Unhandled character set encoding");
							break;
					}
				}
			}
		}
	}
	
	@Override
	public void incomingSearchRequest(APDUEvent event) 
	{
		LOGGER.debug("SEARCH request received.");

		SearchRequest_type searchRequest = (SearchRequest_type) (event.getPDU().o);

		int ssub = searchRequest.smallSetUpperBound.intValue();
		int lslb = searchRequest.largeSetLowerBound.intValue();
		int mspn = searchRequest.mediumSetPresentNumber.intValue();

		String preferredRecordSyntax = null;

		if (searchRequest.preferredRecordSyntax != null) 
		{
			OIDRegisterEntry ent = registry.lookupByOID(searchRequest.preferredRecordSyntax);
			preferredRecordSyntax = (ent != null ? ent.getName() : null);
		}

		String resultsetName = event.getReference();
		resultsetName = (resultsetName != null && resultsetName.trim().length() != 0) ? resultsetName : "default";
		
		backend.search(new BackendSearchDTO(
				this,
				searchRequest.query,
				searchRequest.databaseNames,
				preferredRecordSyntax,
				extractSetname(searchRequest.smallSetElementSetNames),
				extractSetname(searchRequest.mediumSetElementSetNames),
				resultsetName, 
				searchRequest.replaceIndicator
						.booleanValue(), searchRequest.referenceId));
	}

	public void notifySearchResult(BackendSearchDTO searchDTO) 
	{
		LOGGER.debug("Sending back SEARCH response.");

		// Create a search response
		PDU_type pdu = new PDU_type();
		pdu.which = PDU_type.searchresponse_CID;

		SearchResponse_type response = new SearchResponse_type();
		pdu.o = response;

		// response.referenceId = search_request.referenceId;

		// Assume failure unless something below sets to true
		response.searchStatus = Boolean.valueOf(searchDTO.search_status);
		response.resultCount = BigInteger.valueOf(searchDTO.result_count);
		if (searchDTO.refid != null)
		{
			response.referenceId = searchDTO.refid;
		}
		
		if (searchDTO.search_status) 
		{
			if (searchDTO.piggyback_records != null) 
			{
				response.presentStatus = BigInteger.valueOf(0); // OK...
				response.numberOfRecordsReturned = BigInteger.valueOf(searchDTO.piggyback_records.length);
				response.nextResultSetPosition = BigInteger.valueOf(searchDTO.piggyback_records.length + 1);
			} else 
			{
				response.presentStatus = BigInteger.valueOf(0); // OK... (but no records presented)
				response.numberOfRecordsReturned = BigInteger.valueOf(0);
				response.nextResultSetPosition = BigInteger.valueOf(1);
			}
		} else 
		{
			response.presentStatus = BigInteger.valueOf(5); // Failure
			response.numberOfRecordsReturned = BigInteger.valueOf(0);
			response.nextResultSetPosition = BigInteger.valueOf(0);
			response.resultSetStatus = BigInteger.valueOf(3); // No result set available
			if (searchDTO.diagnostic_code == 0) 
			{
				String addinfo = null;
				if (searchDTO.status_report != null)
				{
					addinfo = searchDTO.status_report.toString();
				}
				response.records = createNSD("2", addinfo, searchDTO.diagnostic_data);
			} else
			{
				response.records = createNSD(
						"" + searchDTO.diagnostic_code, 
						searchDTO.diagnostic_addinfo, 
						searchDTO.diagnostic_data);
			}
		}

		// Extra info for meta-search
		// if ( add_extra_status_info && ( bsr.status_report != null ) ) {
		// Create a list in additionalSearchInfo if one doesn't exist
		// if ( response.additionalSearchInfo == null )
		// response.additionalSearchInfo = new ArrayList();
		// Create and add a report for our internal structure
		// createAdditionalSearchInfo(response.additionalSearchInfo,
		// bsr.status_report);
		// }

		LOGGER.debug("Sending back SEARCH response.");

		try 
		{
			endPoint.encodeAndSend(pdu);
		} catch (IOException exception) 
		{
			LOGGER.error("Unable to send back SEARCH response.", exception);
		}
	}

	private String extractSetname(ElementSetNames_type esn_type) 
	{
		String result;
		if ((esn_type != null) && (esn_type.which == ElementSetNames_type.genericelementsetname_CID))
		{
			result = esn_type.o.toString();
		}
		else
		{
			result = "f";
		}
		return result;
	}

	@Override
	public void incomingPresentRequest(APDUEvent event) 
	{
		LOGGER.debug("Received PRESENT request.");

		PresentRequest_type presentRequest = (PresentRequest_type) (event.getPDU().o);

		int start = presentRequest.resultSetStartPoint.intValue();
		int count = presentRequest.numberOfRecordsRequested.intValue();

		String elementSetName = null;
		String resultSetName = presentRequest.resultSetId;
		String recordSyntax = null;

		OIDRegisterEntry ent = registry.lookupByOID(presentRequest.preferredRecordSyntax);
		if (ent != null)
		{
			recordSyntax = ent.getName();
		}
		
		if ((presentRequest.recordComposition != null) && (presentRequest.recordComposition.which == recordComposition_inline9_type.simple_CID)) 
		{
			elementSetName = extractSetname((ElementSetNames_type) presentRequest.recordComposition.o);
		} else 
		{ 
			// Complex
			// CompSpec! We don't handle this yet. NB that there is nothing to
			// stop the procesing
			// happening here, Just set schema and esn. We just need to write
			// the code to pull data
			// out of the pdu.
			elementSetName = "f";
		}

		ArchetypeRecordFormatSpecification archetype = new ArchetypeRecordFormatSpecification(elementSetName);

		ExplicitRecordFormatSpecification explicit = getExplicitFormat(recordSyntax, elementSetName);
		// ExplicitRecordFormatSpecification explicit = new
		// ExplicitRecordFormatSpecification(record_syntax,schema,element_set_name);

		backend.present(new BackendPresentDTO(this,
				resultSetName != null ? resultSetName : "Default", start,
				count, recordSyntax, elementSetName,
				presentRequest.recordComposition, presentRequest.referenceId,
				archetype, explicit));
	}

	public void notifyPresentResult(BackendPresentDTO bpr) {
		LOGGER.debug(
				"Sending back PRESENT response. Start=" 
				+ bpr.start 
				+ ", count=" 
				+ bpr.count 
				+ ", syntax=" 
				+ bpr.record_syntax);
		
		PDU_type pdu = new PDU_type();
		pdu.which = PDU_type.presentresponse_CID;
		PresentResponse_type response = new PresentResponse_type();
		pdu.o = response;
		response.referenceId = bpr.refid;
		response.otherInfo = null;

		// element_set_name =
		// extractSetname(search_request.smallSetElementSetNames);

		if (bpr.start <= 0) 
		{
			LOGGER.debug("Present out of range");
			response.presentStatus = BigInteger.valueOf(5);
			response.numberOfRecordsReturned = BigInteger.valueOf(0);
			response.nextResultSetPosition = BigInteger.valueOf(bpr.next_result_set_position);
			response.records = createNonSurrogateDiagnostic(1,"Unknown internal error presenting result records");
		} else if (bpr.start > bpr.total_hits) 
		{
			LOGGER.debug("Present out of range");
			response.presentStatus = BigInteger.valueOf(5);
			response.numberOfRecordsReturned = BigInteger.valueOf(0);
			response.nextResultSetPosition = BigInteger.valueOf(bpr.next_result_set_position);
			response.records = createNonSurrogateDiagnostic(13, 
					"Requested start record " 
					+ bpr.start 
					+ ", only 1 to "
					+ bpr.total_hits + " available");
		} else 
		{
			response.records = createRecordsFor(bpr.result_records, bpr.record_syntax);

			// response.records = createRecordsFor(bpr.result_records,
			// bpr.explicit);
			response.nextResultSetPosition = BigInteger.valueOf(bpr.next_result_set_position);

			if ((response.records != null) && (response.records.which == Records_type.responserecords_CID)) 
			{
				// Looks like we managed to present some records OK..
				LOGGER.debug("Got some records to present " + ((List) (response.records.o)).size());
				response.numberOfRecordsReturned = BigInteger.valueOf(((List) (response.records.o)).size());
				response.presentStatus = BigInteger.valueOf(0);
			} else 
			{ 
				// Non surrogate diagnostics ( Single or Multiple )
				LOGGER.debug("response.records.which did not contain response records.. Non surrogate diagnostic returned");
				response.numberOfRecordsReturned = BigInteger.valueOf(0);
				// PresentStatus, 0=0K, 1,2,3,4 = Partial, 5=Failure, None
				// surrogate diag
				response.presentStatus = BigInteger.valueOf(5);
			}
		}

		try 
		{
			endPoint.encodeAndSend(pdu);
		} catch (IOException exception) 
		{
			LOGGER.error("Unable to send back PRESENT response.", exception);
		}
	}

	public void incomingDeleteResultSetRequest(APDUEvent event) 
	{
		LOGGER.debug("Received DELETERESULTSET request.");

		DeleteResultSetRequest_type deleteRequest = (DeleteResultSetRequest_type) (event.getPDU().o);

		PDU_type pdu = new PDU_type();
		pdu.which = PDU_type.deleteresultsetresponse_CID;
		DeleteResultSetResponse_type response = new DeleteResultSetResponse_type();
		pdu.o = response;
		response.referenceId = deleteRequest.referenceId;

		if (deleteRequest.deleteFunction.intValue() == 0) 
		{
			// Delete the result sets identified by delete_request.resultSetList
			for (Iterator task_list = deleteRequest.resultSetList.iterator(); task_list.hasNext();) 
			{
				String next_rs = (String) task_list.next();
				// AbstractIRResultSet st =
				// (AbstractIRResultSet)(active_searches.get(next_rs));
				// active_searches.remove(next_rs);
				// search_service.deleteTask(st.getTaskIdentifier());
			}
		} else 
		{
			// Function must be 1 : All sets in the association
			// active_searches.clear();
		}

		response.deleteOperationStatus = BigInteger.valueOf(0); // 0 = Success,
																// 1=resultSetDidNotExist,
																// 2=previouslyDeletedByTarget,
																// 3=systemProblemAtTarget
																// 4 =
																// accessNotAllowed,
																// 5=resourceControlAtOrigin
		try 
		{
			endPoint.encodeAndSend(pdu);
		} catch (java.io.IOException exception) 
		{
			LOGGER.error("Unable to send back PRESENT response.", exception);
		}
	}

	public void notifyDeleteResult(BackendDeleteDTO bdr) 
	{
		LOGGER.debug(bdr);
	}

	public void incomingAccessControlRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming accessControlRequest");
	}

	@Override
	public void incomingAccessControlResponse(APDUEvent e) 
	{
		LOGGER.info("Incoming AccessControlResponse");
	}

	@Override
	public void incomingResourceControlRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming resourceControlRequest");
	}

	@Override
	public void incomingTriggerResourceControlRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming triggetResourceControlRequest");
	}
	
	@Override
	public void incomingResourceReportRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming resourceReportRequest");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void incomingScanRequest(APDUEvent event) 
	{
		ScanRequest_type scanRequest = (ScanRequest_type) (event.getPDU().o);
		try 
		{
			if (scanService != null)
			{
				String name = null;

				OIDRegisterEntry entry = registry.lookupByOID(scanRequest.attributeSet);
				if (entry != null) 
				{
					name = entry.getName();
				}

				int i1 = (scanRequest.stepSize == null ? 0 : scanRequest.stepSize.intValue());
				int i2 = (scanRequest.numberOfTermsRequested == null ? 0 : scanRequest.numberOfTermsRequested.intValue());
				int i3 = (scanRequest.preferredPositionInResponse == null ? 0 : scanRequest.preferredPositionInResponse.intValue());

				ScanRequestInfo sri = new ScanRequestInfo();
				sri.collections = new Vector(scanRequest.databaseNames);
				sri.attribute_set = name;

				List<AttributeElement_type> attributes = (ArrayList<AttributeElement_type>) scanRequest.termListAndStartPoint.attributes;

				// Determining request object
				// 1. Attribute(s)
				AttrPlusTermNode atpn = new AttrPlusTermNode();
				if (attributes != null && !attributes.isEmpty()) 
				{
					AttributeElement_type description = attributes.get(0);
					atpn.setAccessPoint(new AttrValue(
							name,
							description.attributeType
									+ "."
									+ String.valueOf(description.attributeValue.o)));
				} else 
				{
					String defaultScanAccessPoint = (String) browseServiceDefinition.getPreference("defaultScanAccessPoint", "bib-1.1.4");
					
					LOGGER.debug("No access point has been specified...using a default value of "+ defaultScanAccessPoint);

					atpn.setAccessPoint(new AttrValue(name,defaultScanAccessPoint));
				}

				// 2. Value

				atpn.setTerm(new String(
						((byte[]) scanRequest.termListAndStartPoint.term.o)));
				sri.term_list_and_start_point = atpn;
				sri.step_size = i1;
				sri.number_of_terms_requested = i2;
				sri.position_in_response = i3;

				ScanInformation scan_result = scanService.doScan(sri);

				endPoint.sendScanResponse(scanRequest.referenceId,
						BigInteger.valueOf(i1), ScanStatus.SUCCESS,
						BigInteger.valueOf(scan_result.position),
						scan_result.results, scanRequest.attributeSet, null);
			} else 
			{
				endPoint.sendScanResponse(
						scanRequest.referenceId,
						BigInteger.ZERO, 
						ScanStatus.FAILURE,
						BigInteger.ZERO,
						null, 
						scanRequest.attributeSet, null);
			}
		} catch (Exception exception) 
		{
			LOGGER.debug("Error while attempting to execute a SCAN request.", exception);
			
			try 
			{
				endPoint.sendScanResponse(scanRequest.referenceId,
						BigInteger.valueOf(0), BigInteger.valueOf(6),
						BigInteger.valueOf(0), new Vector(),
						scanRequest.attributeSet, 
						null);
			} catch (IOException ignore) 
			{
				LOGGER.error("Unable to send back SCAN response.", exception);
			}
		} 
	}

	@Override
	public void incomingSortRequest(APDUEvent event) 
	{
		LOGGER.debug("Incoming sortRequest");
		SortRequest_type sortRequest = (SortRequest_type) (event.getPDU().o);
		backend.sort(new BackendSortDTO(sortRequest));

		// Create a DeleteResultSetResponse
		PDU_type pdu = new PDU_type();
		pdu.which = PDU_type.sortresponse_CID;
		SortResponse_type response = new SortResponse_type();
		pdu.o = response;
		response.referenceId = sortRequest.referenceId;

		response.sortStatus = BigInteger.valueOf(0); // 0 = Success,
																// 1=resultSetDidNotExist,
																// 2=previouslyDeletedByTarget,
																// 3=systemProblemAtTarget
																// 4 =
																// accessNotAllowed,
																// 5=resourceControlAtOrigin
		try 
		{
			endPoint.encodeAndSend(pdu);
		} catch (IOException exception) 
		{
			LOGGER.error("Unable to send back SORT response.", exception);
		}
	}

	@Override
	public void incomingSegmentRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming segmentRequest");
	}

	@Override
	public void incomingExtendedServicesRequest(APDUEvent e) 
	{
		LOGGER.info("Incoming extendedServicesRequest");
	}

	@Override
	public void incomingClose(APDUEvent e) 
	{
		LOGGER.debug("Close...");
		endPoint.getPDUAnnouncer().deleteObserver(eventAdapter);
		endPoint.getPDUAnnouncer().deleteObservers();
		eventAdapter = null;
		endPoint.shutdown();

		try 
		{
			endPoint.join();
		} catch (InterruptedException ie) {
		}
		
		LOGGER.debug("Done joining with assoc thread");

		LOGGER.debug("Deleting tasks...");
		endPoint = null;
	}

	private Records_type createNonSurrogateDiagnostic(int condition, String addinfo) 
	{
		Records_type retval = new Records_type();
		retval.which = Records_type.nonsurrogatediagnostic_CID;
		DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
		retval.o = default_diag;
		default_diag.diagnosticSetId = registry.oidByName("diag-bib-1");
		default_diag.condition = BigInteger.valueOf(condition);
		if (addinfo != null) {
			default_diag.addinfo = new addinfo_inline14_type();
			default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
			default_diag.addinfo.o = (Object) (addinfo);
		}
		return retval;
	}

	// Helper functions
	//
	//
	private Records_type createRecordsFor(InformationFragment[] raw_records,
			String spec) {

		LOGGER.debug("createRecordsFor... spec=" + spec);
		RecordBuilder rb = null; // (RecordBuilder)
									// ctx.getBean(spec.toLowerCase());

		Records_type retval = new Records_type();
		try {
			if (rb != null) {
				LOGGER.error("createRecordsFor called with unhandled record syntax");
				retval.which = Records_type.nonsurrogatediagnostic_CID;
				DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
				retval.o = default_diag;
				default_diag.diagnosticSetId = registry.oidByName("diag-bib-1");
				default_diag.condition = BigInteger.valueOf(0);
				default_diag.addinfo = new addinfo_inline14_type();
				default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
				default_diag.addinfo.o = (Object) ("createRecordsFor called with unhandled record syntax");
			} else if (raw_records != null) {
				ArrayList v = new ArrayList();
				retval.which = Records_type.responserecords_CID;
				retval.o = v;

				for (int i = 0; i < raw_records.length; i++) {
					LOGGER.debug("Adding record " + i + " to result");

					NamePlusRecord_type npr = new NamePlusRecord_type();
					npr.name = raw_records[i].getSourceCollectionName();
					npr.record = new record_inline13_type();

					try {
						// InformationFragment frag_in_req_format =
						// rf.buildFrom(raw_records[i]);
						npr.record.which = record_inline13_type.retrievalrecord_CID;
						// npr.record.o =
						// encodeRecordForZ3950(frag_in_req_format);
						npr.record.o = encodeRecordForZ3950(raw_records[i]);
					} catch (Exception e) {
						LOGGER.warn("Problem encoding respionse fragment", e);
						npr.record.which = record_inline13_type.surrogatediagnostic_CID;
						DiagRec_type diag_rec = new DiagRec_type();
						DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
						default_diag.diagnosticSetId = registry
								.oidByName("diag-bib-1");
						default_diag.condition = BigInteger.valueOf(0);
						default_diag.addinfo = new addinfo_inline14_type();
						default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
						default_diag.addinfo.o = (Object) (e.toString());
						diag_rec.which = DiagRec_type.defaultformat_CID;
						diag_rec.o = default_diag;
						npr.record.o = diag_rec;
					}
					v.add(npr);
				}
			} else {
				LOGGER.error("createRecordsFor called with no internal records");
				retval.which = Records_type.nonsurrogatediagnostic_CID;
				DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
				retval.o = default_diag;
				default_diag.diagnosticSetId = registry.oidByName("diag-bib-1");
				default_diag.condition = BigInteger.valueOf(0);
				default_diag.addinfo = new addinfo_inline14_type();
				default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
				default_diag.addinfo.o = (Object) ("createRecordsFor called with no internal records");
			}
		} catch (Exception pe) {
			LOGGER.error("Error processing records in createRecordsFor.." + spec,
					pe);
			// Need to set up diagnostic in here
			retval.which = Records_type.nonsurrogatediagnostic_CID;
			DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
			retval.o = default_diag;
			default_diag.diagnosticSetId = registry.oidByName("diag-bib-1");

			// if ( pe.additional != null )
			// default_diag.condition = BigInteger.valueOf(
			// Long.parseLong(pe.additional.toString()) );
			// else
			default_diag.condition = BigInteger.valueOf(0);

			default_diag.addinfo = new addinfo_inline14_type();
			default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
			default_diag.addinfo.o = (Object) (pe.toString());
		}

		return retval;
	}

	private EXTERNAL_type encodeRecordForZ3950(InformationFragment fragment) {

		LOGGER.debug("encodeRecordForZ3950... encoding="
				+ fragment.getFormatSpecification().getEncoding() + ", schema="
				+ fragment.getFormatSpecification().getSchema().toString());

		EXTERNAL_type rec = null;

		if (fragment.getOriginalObject() instanceof Document) {
			rec = new EXTERNAL_type();
			rec.direct_reference = registry.oidByName("xml");
			rec.encoding = new encoding_inline0_type();
			rec.encoding.which = encoding_inline0_type.octet_aligned_CID;
			try {
				Document d = (Document) fragment.getOriginalObject();
				LOGGER.debug("serialize " + d);
				OutputFormat format = new OutputFormat("xml", "utf-8", false);
				format.setOmitXMLDeclaration(true);
				java.io.StringWriter stringOut = new java.io.StringWriter();
				XMLSerializer serial = new XMLSerializer(stringOut, format);
				serial.setNamespaces(true);
				serial.asDOMSerializer();
				serial.serialize(d.getDocumentElement());
				rec.encoding.o = stringOut.toString().getBytes("UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
				rec.encoding.o = new String(e.toString()).getBytes();
			}
		} else if (fragment.getFormatSpecification().getEncoding().toString()
				.equals("iso2709")) {
			rec = new EXTERNAL_type();
			rec.direct_reference = registry.oidByName(fragment
					.getFormatSpecification().getSchema().toString());
			rec.encoding = new encoding_inline0_type();
			rec.encoding.which = encoding_inline0_type.octet_aligned_CID;
			rec.encoding.o = (byte[]) (fragment.getOriginalObject());
		} else if (fragment.getOriginalObject() instanceof String) {
			rec = new EXTERNAL_type();
			rec.direct_reference = registry.oidByName("sutrs");
			rec.encoding = new encoding_inline0_type();
			rec.encoding.which = encoding_inline0_type.single_asn1_type_CID;
			rec.encoding.o = fragment.toString();
		} else {
			throw new RuntimeException("unhandled object encoding: "
					+ fragment.getFormatSpecification());
		}

		return rec;
	}

	private Records_type createNSD(String diag_code, String additional,
			Object[] params) {
		Records_type retval = new Records_type();
		retval.which = Records_type.nonsurrogatediagnostic_CID;
		DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
		retval.o = default_diag;

		default_diag.diagnosticSetId = registry.oidByName("diag-bib-1");

		if (diag_code != null)
			default_diag.condition = BigInteger.valueOf(Long
					.parseLong(diag_code));
		else
			default_diag.condition = BigInteger.valueOf(0);

		if (additional != null) {
			default_diag.addinfo = new addinfo_inline14_type();
			default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
			default_diag.addinfo.o = (Object) (additional);
		}

		return retval;
	}

	private ExplicitRecordFormatSpecification getExplicitFormat(
			String record_syntax, String element_set_name) {

		ExplicitRecordFormatSpecification result = null;

		if (record_syntax.equalsIgnoreCase("usmarc")) {
			result = new ExplicitRecordFormatSpecification("iso2709", "usmarc",
					element_set_name);
		} else if (record_syntax.equalsIgnoreCase("marc21")) {
			result = new ExplicitRecordFormatSpecification("iso2709", "marc21",
					element_set_name);
		} else if (record_syntax.equalsIgnoreCase("ukmark")) {
			result = new ExplicitRecordFormatSpecification("iso2709", "ukmarc",
					element_set_name);
		} else if (record_syntax.equalsIgnoreCase("sutrs")) {
			result = new ExplicitRecordFormatSpecification("string", "sutrs",
					element_set_name);
		} else if (record_syntax.equalsIgnoreCase("xml")) {
			result = new ExplicitRecordFormatSpecification("xml",
					element_set_name, null);
		}

		return result;
	}

	private void createAdditionalSearchInfo(List l,
			BackendStatusReportDTO backend_status_report) {
		String report = backend_status_report.toString();
		OtherInformationItem43_type result = new OtherInformationItem43_type();
		result.information = new information_inline44_type();
		result.information.which = information_inline44_type.characterinfo_CID;
		result.information.o = report;
		l.add(result);
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.applicationContext = ctx;
	}
}
