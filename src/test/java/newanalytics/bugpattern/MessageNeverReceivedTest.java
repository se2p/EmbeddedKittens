package newanalytics.bugpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import newanalytics.IssueReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.Program;
import scratch.ast.parser.ProgramParser;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

class MessageNeverReceivedTest {
    private static Program program;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/bugpattern/broadcastSync.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            program = ProgramParser.parseProgram("broadcastSync", objectMapper.readTree(file));
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testMessageNeverReceived() {
        MessageNeverReceived finder = new MessageNeverReceived();
        final IssueReport check = finder.check(program);
        Truth.assertThat(check.getCount()).isEqualTo(4);
        Truth.assertThat(check.getPosition().get(2)).isEqualTo("Apple");
        Truth.assertThat(check.getPosition().get(0)).isEqualTo("Sprite1");
        Truth.assertThat(check.getPosition().get(1)).isEqualTo("Abby");
    }
}