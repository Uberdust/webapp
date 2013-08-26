package eu.uberdust.rest.controller.html.testbed;

import eu.uberdust.caching.Cachable;
import eu.uberdust.caching.Loggable;
import eu.wisebed.wisedb.controller.NodeController;
import eu.wisebed.wisedb.controller.TestbedController;
import eu.wisebed.wisedb.model.Node;
import eu.wisebed.wisedb.model.Origin;
import eu.wisebed.wisedb.model.Position;
import eu.wisebed.wisedb.model.Testbed;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class that returns a list of testbed in HTML format.
 */
@Controller
@RequestMapping(value = {"/", "/testbed"})
public final class ListTestbedsController {

    /**
     * Testbed persistence manager.
     */

    private transient TestbedController testbedManager;
    private transient NodeController nodeManager;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ListTestbedsController.class);

    /**
     * Sets testbed persistence manager.
     *
     * @param testbedManager testbed persistence manager.
     */
    @Autowired
    public void setTestbedManager(final TestbedController testbedManager) {
        this.testbedManager = testbedManager;
    }

    @Autowired
    public void setNodeManager(final NodeController nodeManager) {
        this.nodeManager = nodeManager;
    }

    /**
     * Handle Request and return the appropriate response.
     *
     * @return response http servlet response.
     */
    @Loggable
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView listTestbeds() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (user instanceof User) {
            username = ((User) user).getUsername();
        } else {
            username = null;
        }
        LOGGER.info("username:" + username);
        try {


            final long start = System.currentTimeMillis();

            // Prepare data to pass to jsp
            final Map<String, Object> refData = new HashMap<String, Object>();


            // testbed list
            final List<Testbed> testbeds = testbedManager.list();
            if (testbeds.size() == 0) {
                return new ModelAndView("testbed/add.html", refData);
            }

            Map<Integer, Origin> origins = new HashMap<Integer, Origin>();
            List<Position> nodePositions = new ArrayList<Position>();
            for (Testbed testbed : testbeds) {
                origins.put(testbed.getId(), testbed.getSetup().getOrigin());
                Map<String, Position> testbedNodePositions = getNodePositions(testbed);
                nodePositions.addAll(testbedNodePositions.values());
            }


            final Map<String, Long> nodesCount = testbedManager.countNodes();
            final Map<String, Long> linksCount = testbedManager.countLinks();

            refData.put("testbeds", testbeds);
            refData.put("nodes", nodesCount);
            refData.put("links", linksCount);
            refData.put("origins", origins);
            refData.put("nodePositions", nodePositions);
            refData.put("username", username);

            refData.put("time", String.valueOf((System.currentTimeMillis() - start)));
            return new ModelAndView("testbed/list.html", refData);
        } catch (Exception e) {
            LOGGER.error("e", e);
        }
        return null;
    }

    @Cachable
    private Map<String, Position> getNodePositions(Testbed testbed) {
        final List<Node> nodes = nodeManager.list(testbed.getSetup());
        Map<String, Position> nodePositions = new HashMap<String, Position>();
        for (Node node : nodes) {
            Position nodePosition = nodeManager.getAbsolutePosition(node);
            nodePositions.put(node.getName(), nodePosition);
        }
        return nodePositions;
    }
}
