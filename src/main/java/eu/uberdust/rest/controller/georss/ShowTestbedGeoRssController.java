package eu.uberdust.rest.controller.georss;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.SimpleModuleImpl;
import com.sun.syndication.feed.module.georss.geometries.Position;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import eu.uberdust.command.TestbedCommand;
import eu.uberdust.rest.exception.InvalidTestbedIdException;
import eu.uberdust.rest.exception.TestbedNotFoundException;
import eu.uberdust.util.Coordinate;
import eu.wisebed.wisedb.controller.NodeCapabilityController;
import eu.wisebed.wisedb.controller.NodeController;
import eu.wisebed.wisedb.controller.TestbedController;
import eu.wisebed.wisedb.model.Node;
import eu.wisebed.wisedb.model.NodeCapability;
import eu.wisebed.wisedb.model.Origin;
import eu.wisebed.wisedb.model.Testbed;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractRestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller class that returns the setup of a testbed in GeoRSS format.
 */
public final class ShowTestbedGeoRssController extends AbstractRestController {

    /**
     * Testbed persistence manager.
     */
    private transient TestbedController testbedManager;


    public NodeController getNodeManager() {
        return nodeManager;
    }

    public void setNodeManager(NodeController nodeManager) {
        this.nodeManager = nodeManager;
    }

    private transient NodeController nodeManager;

    private transient NodeCapabilityController nodeCapabilityManager;

    public void setNodeCapabilityManager(NodeCapabilityController nodeCapabilityManager) {
        this.nodeCapabilityManager = nodeCapabilityManager;
    }

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ShowTestbedGeoRssController.class);

    /**
     * Constructor.
     */
    public ShowTestbedGeoRssController() {
        super();

        // Make sure to set which method this controller will support.
        this.setSupportedMethods(new String[]{METHOD_GET});
    }

    /**
     * Sets testbed persistence manager.
     *
     * @param testbedManager testbed persistence manager.
     */
    public void setTestbedManager(final TestbedController testbedManager) {
        this.testbedManager = testbedManager;
    }

    /**
     * Handle request and return the appropriate response.
     *
     * @param request    http servlet request.
     * @param response   http servlet response.
     * @param commandObj command object.
     * @param errors     a BindException exception.
     * @return http servlet response.
     * @throws TestbedNotFoundException  a TestbedNotFoundException exception.
     * @throws InvalidTestbedIdException a InvalidTestbedIdException exception.
     * @throws IOException               a IOException exception.
     * @throws FeedException             a FeedException exception.
     */
    @SuppressWarnings("unchecked")
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                                  final Object commandObj, final BindException errors)
            throws TestbedNotFoundException, InvalidTestbedIdException, IOException, FeedException {

        // set command object
        final TestbedCommand command = (TestbedCommand) commandObj;

        // a specific testbed is requested by testbed Id
        int testbedId;
        try {
            testbedId = Integer.parseInt(command.getTestbedId());

        } catch (NumberFormatException nfe) {
            throw new InvalidTestbedIdException("Testbed IDs have number format.", nfe);
        }

        // look up testbed
        final Testbed testbed = testbedManager.getByID(Integer.parseInt(command.getTestbedId()));
        if (testbed == null) {
            // if no testbed is found throw exception
            throw new TestbedNotFoundException("Cannot find testbed [" + testbedId + "].");
        }

        // set up feed and entries
        response.setContentType("application/xml; charset=UTF-8");
        final SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle(testbed.getName() + " GeoRSS");
        feed.setLink(request.getRequestURL().toString());
        feed.setDescription(testbed.getDescription());
        final List<SyndEntry> entries = new ArrayList<SyndEntry>();

        // convert testbed origin from long/lat position to xyz if needed
        Coordinate properOrigin = null;
        if (!(testbed.getSetup().getCoordinateType().equals("Absolute"))) {
            // determine testbed origin by the type of coordinates given
            final Origin origin = testbed.getSetup().getOrigin();
            final Coordinate originCoordinate = new Coordinate((double) origin.getX(), (double) origin.getY(),
                    (double) origin.getZ(), (double) origin.getPhi(), (double) origin.getTheta());
            properOrigin = Coordinate.blh2xyz(originCoordinate);
        }

        // current host base URL
        final String baseUrl = (request.getRequestURL().toString()).replace(request.getRequestURI(), "");
        LOGGER.info(baseUrl);

        // list of nodes
        final List<Node> nodes = nodeManager.list(testbed);

        // make an entry and it
        for (Node node : nodes) {
            final SyndEntry entry = new SyndEntryImpl();

            // set entry's title,link and publishing date
            entry.setTitle(node.getId());
            entry.setLink(new StringBuilder().append(baseUrl).append("/rest/testbed/")
                    .append(testbed.getId()).append("/node/").append(node.getId()).toString());
            entry.setPublishedDate(new Date());

            // set entry's description (HTML list)
            final SyndContent description = new SyndContentImpl();
            final StringBuilder descriptionBuffer = new StringBuilder();
            descriptionBuffer.append("<p>").append(nodeManager.getDescription(node)).append("</p>");
            descriptionBuffer.append("<p><a href=\"").append(baseUrl).append("/uberdust/rest/testbed/")
                    .append(testbed.getId()).append("/node/").append(node.getId()).append("/georss").append("\">")
                    .append("GeoRSS feed").append("</a></p>");
            descriptionBuffer.append("<ul>");
            for (NodeCapability capability : (List<NodeCapability>) nodeCapabilityManager.list(node)) {
                descriptionBuffer.append("<li>").append(capability.getCapability().getName())
                        .append(capability.getCapability().getName()).append("</li>");
            }
            descriptionBuffer.append("</ul>");
            description.setType("text/html");
            description.setValue(descriptionBuffer.toString());
            entry.setDescription(description);


            // set the GeoRSS module and add it to entry
            final GeoRSSModule geoRSSModule = new SimpleModuleImpl();
            if (!(testbed.getSetup().getCoordinateType().equals("Absolute"))) {
                // convert node position from xyz to long/lat
                final eu.wisebed.wiseml.model.setup.Position position = new eu.wisebed.wiseml.model.setup.Position();
                final Origin npos = nodeManager.getOrigin(node);
                position.setPhi(npos.getPhi());
                position.setTheta(npos.getTheta());
                position.setX(npos.getX());
                position.setY(npos.getY());
                position.setZ(npos.getZ());

                final Coordinate nodeCoordinate = new Coordinate((double) position.getX(), (double) position.getY(),
                        (double) position.getZ());
                final Coordinate rotated = Coordinate.rotate(nodeCoordinate, properOrigin.getPhi());
                final Coordinate absolute = Coordinate.absolute(properOrigin, rotated);
                final Coordinate nodePosition = Coordinate.xyz2blh(absolute);
                geoRSSModule.setPosition(new Position(nodePosition.getX(), nodePosition.getY()));
            } else {
                geoRSSModule.setPosition(new Position(nodeManager.getOrigin(node).getX(), nodeManager.getOrigin(node).getY()));
            }
            entry.getModules().add(geoRSSModule);
            entries.add(entry);
        }

        // add entries to feed
        feed.setEntries(entries);

        // the feed output goes to response
        final SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, response.getWriter());

        return null;
    }
}