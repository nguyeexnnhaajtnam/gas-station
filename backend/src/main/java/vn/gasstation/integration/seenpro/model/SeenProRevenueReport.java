package vn.gasstation.integration.seenpro.model;
import java.util.List;
public record SeenProRevenueReport(List<String> todayValues,List<SeenProRevenueRow> fuels,List<SeenProRevenueRow> pumps,List<SeenProRevenueRow> invoices,List<SeenProChartSeries> fuelChart,List<SeenProChartSeries> pumpChart) {public record SeenProRevenueRow(String name,String liters,String revenue,String count) {}public record SeenProChartSeries(String name,List<String> labels,List<String> values) {}}
