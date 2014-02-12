package subset;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by blackbriar on 2/11/14.
 */
public class SubsetMatch {
    private final int[] side1Idxs;
    private final int[] side2Idxs;
    private final long[] sums;

    public SubsetMatch(int[] side1Idxs, int[] side2Idxs, long[] sums) {
        this.side1Idxs = side1Idxs;
        this.side2Idxs = side2Idxs;
        this.sums = sums;
    }

    public int getTotalNoOfTransactions() {
        return side1Idxs.length + side2Idxs.length;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("Transactions", getTotalNoOfTransactions())
                .append("side1Idxs", side1Idxs)
                .append("side2Idxs", side2Idxs)
                .append("sums", sums)
                .toString();
    }
}
