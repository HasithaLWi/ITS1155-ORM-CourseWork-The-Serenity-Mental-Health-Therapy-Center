package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JasperReportUtil {

    public static void printInvoice(Long paymentId) {
        if (paymentId == null) {
            System.err.println("Cannot print invoice: paymentId is null");
            return;
        }
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            session.doWork(connection -> {
                try {
                    InputStream reportStream = JasperReportUtil.class.getResourceAsStream(
                            "/lk/ijse/theserenitymentalhealththerapycenter/report/invoice.jrxml"
                    );
                    if (reportStream == null) {
                        throw new JRRuntimeException("Invoice template not found on classpath.");
                    }
                    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("payment_id", paymentId);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                    JasperViewer.viewReport(jasperPrint, false);
                } catch (JRException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error filling invoice report: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printFinancialReport(Map<String, Object> parameters) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            session.doWork(connection -> {
                try {
                    InputStream reportStream = JasperReportUtil.class.getResourceAsStream(
                            "/lk/ijse/theserenitymentalhealththerapycenter/report/financial_report.jrxml"
                    );
                    if (reportStream == null) {
                        throw new JRRuntimeException("Financial report template not found on classpath.");
                    }
                    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
                    JasperViewer.viewReport(jasperPrint, false);
                } catch (JRException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error filling financial report: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
