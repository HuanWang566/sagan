package cn.edu.sjtu.ipads.wbridge.servlet;

import cn.edu.sjtu.ipads.wbridge.WBridgeBaseEntryPoint;
import org.springframework.ui.ExtendedModelMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class GetBlogServerlet implements Servlet {
    @Override
    public void init(ServletConfig config) {}

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) {
        // TODO mark these parameter symbolic. if these parameters are used in filter, we need to mark
        // them as symbolic at the start of the filter
        System.out.println("End of filter chain reached, build parameter.");
        ExtendedModelMap model = new ExtendedModelMap();
        // TODO: it seems we need to mark test_page symbolic
        final int test_page = 1;
        // HttpServletResponse response = (HttpServletResponse) res;
        WBridgeBaseEntryPoint.blogController.listPublishedPosts(
                    model,  test_page);
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {}
}
