package subset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.*;

/**
 * Created by blackbriar on 2/11/14.
 */
public class SubsetMatching {

    public static final String PRODUCT_FIELD = "product";
    public static final String QUANTITY_FIELD = "quantity";

    public static SubsetMatch performMatching(List<Map<String, Object>> side1, List<Map<String, Object>> side2, String... aggrFields) {
        Set<SubsetGenerator.Subset> subsetsSide1 = new SubsetGenerator(aggrFields, side1).generateSubsets();
        Set<SubsetGenerator.Subset> subsetsSide2 = new SubsetGenerator(aggrFields, side2).generateSubsets();

        SubsetMatch currentBest = new SubsetMatch(new int[0], new int[0], new long[aggrFields.length]);
        Iterator<SubsetGenerator.Subset> s1It = subsetsSide1.iterator();
        Iterator<SubsetGenerator.Subset> s2It = subsetsSide2.iterator();
        System.out.printf("Generated subsets.S1: %d, s2: %d.", subsetsSide1.size(), subsetsSide2.size());
        try {
            System.in.read();
        } catch (IOException e) {


        }
        SubsetGenerator.Subset side1Sub = null;
        SubsetGenerator.Subset side2Sub =  subsetsSide2.iterator().next();
        while (s1It.hasNext() && s2It.hasNext()) {
            side1Sub = nextBiggerElement(s1It, side2Sub);
            side2Sub = nextBiggerElement(s2It, side1Sub);

            if (side1Sub.equals(side2Sub)) {
                SubsetMatch newMatch = new SubsetMatch(side1Sub.elementsIdxs, side2Sub.elementsIdxs, side1Sub.sums);
                if (currentBest.getTotalNoOfTransactions() < newMatch.getTotalNoOfTransactions()) {
                    currentBest = newMatch;
                }
            }


        }

        return currentBest;
    }

    private static SubsetGenerator.Subset nextBiggerElement(Iterator<SubsetGenerator.Subset> subsetIter, SubsetGenerator.Subset elemToCompare) {
        SubsetGenerator.Subset currElem = subsetIter.next();
        while (currElem.compareTo(elemToCompare) < 0) {
            if (subsetIter.hasNext())
                currElem = subsetIter.next();
            else {
                currElem = null;
                break;
            }
        }
        return currElem;
    }

    public static Map<String, Object> createRow(String product, long quantity) {
        return ImmutableMap.<String, Object>of(PRODUCT_FIELD, product, QUANTITY_FIELD, quantity);
    }

    public static void main(String[] args) {
        List<Map<String, Object>> side1 = createSet(35, 500000);


        List<Map<String, Object>> side2 = createSet(40, 500000);

        SubsetMatch subsetMatch = performMatching(side1, side2, QUANTITY_FIELD);
        System.out.println("BEst match out there: " + subsetMatch);

    }

    private static List<Map<String, Object>> createSet(int numOfElem, int var) {
        Random rand = new Random();
        List<Map<String, Object>> side1 = new ArrayList<>();
        for (int i = 0; i < numOfElem; i++) {
            side1.add((createRow("IBM", rand.nextInt() % var)));
        }
        return side1;
    }
}
