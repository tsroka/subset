package subset;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

public class SubsetGenerator {
    private static final int MAX_SUBSUMS = 1048576; //2^20 = 1 millions

    private final String[] aggregationFields;
    private final int noOfFields;
    private final List<Map<String, Object>> elements;

    public SubsetGenerator(String[] aggregationFields, List<Map<String, Object>> elements) {
        this.aggregationFields = aggregationFields;
        this.elements = elements;
        this.noOfFields = aggregationFields.length;
    }

    private long getFieldAt(int row, String field) {
        return (Long) elements.get(row).get(field);
    }


    public Set<Subset> generateSubsets() {
        TreeMap<Subset, Subset> subsets = new TreeMap<>();

        subsets.put(new Subset(), new Subset());
        outer:
        for (int elNo = 0; elNo < elements.size(); elNo++) {
            TreeSet<Subset> tmp = new TreeSet<>();
            for (Map.Entry<Subset, Subset> parent : subsets.entrySet()) {
                tmp.add(new Subset(elNo, parent.getValue()));
            }

            System.out.printf("Elem no: %d, subset size: %d\n", elNo, subsets.size());
            //Merge
            for (Subset newElem : tmp) {
                Subset existing = subsets.get(newElem);
                if (existing == null) {
                    if (subsets.size() > MAX_SUBSUMS)
                        break outer;
                    //No same elements in existing subset, add new one
                    subsets.put(newElem, newElem);
                } else if (existing.elementsIdxs.length < newElem.elementsIdxs.length) {
                    //Update only if new one has more trabsactions if old one
                    subsets.put(newElem, newElem);
                }
            }
        }
        return subsets.keySet();
    }


    public final class Subset implements Comparable<Subset> {
        final int[] elementsIdxs;
        final long[] sums;


        /**
         * Creates an empty subset
         */
        public Subset() {
            elementsIdxs = new int[0];
            sums = new long[noOfFields];
        }

        /**
         * Create subset from parent by adding additional element
         *
         * @param newElem
         * @param parent
         */
        public Subset(int newElemIdx, Subset parent) {
            int parLen = parent.elementsIdxs.length;
            elementsIdxs = new int[parLen + 1];
            System.arraycopy(parent.elementsIdxs, 0, elementsIdxs, 0, parLen);
            elementsIdxs[parLen] = newElemIdx;
            sums = new long[aggregationFields.length];
            System.arraycopy(parent.sums, 0, sums, 0, noOfFields);
            for (int i = 0; i < noOfFields; i++) {
                sums[i] += getFieldAt(newElemIdx, aggregationFields[i]);
            }
        }


        @Override
        public boolean equals(Object obj) {
            return compareTo((Subset) obj) == 0;
        }

        @Override
        public int compareTo(Subset o) {
            for (int i = 0; i < noOfFields; i++) {
                long diff = sums[i] - o.sums[i];
                if (diff != 0) {
                    return (int) diff;
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("elementsIdxs", elementsIdxs)
                    .append("sums", sums)
                    .toString();
        }
    }
}
