package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.EstoqueLimiteExcedidoException;
import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.exception.QuantidadeInvalidaException;
import br.com.mercado.precificacao.core.model.EntradaEstoque;
import br.com.mercado.precificacao.core.model.NovaEntradaEstoque;
import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.RegistrarEntradaEstoqueOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegistrarEntradaEstoqueUseCaseTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 5);

    private BuscarProdutoOutputPort buscarProdutoOutputPort;
    private RegistrarEntradaEstoqueOutputPort registrarEntradaOutputPort;
    private RegistrarEntradaEstoqueUseCase useCase;

    @BeforeEach
    void setUp() {
        buscarProdutoOutputPort = mock(BuscarProdutoOutputPort.class);
        registrarEntradaOutputPort = mock(RegistrarEntradaEstoqueOutputPort.class);
        Clock clock = Clock.fixed(HOJE.atTime(23, 30).atZone(ZONA).toInstant(), ZONA);
        useCase = new RegistrarEntradaEstoqueUseCase(
                buscarProdutoOutputPort, registrarEntradaOutputPort, new ProdutoService(), clock);
    }

    @Test
    void deveRegistrarEntradaERetornarValoresAnterioresEAtuais() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(
                Optional.of(produto("18.500", "6.90")), Optional.of(produto("31.000", "7.35")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        EntradaEstoque entrada = useCase.executar(15L, new BigDecimal("12.5"), new BigDecimal("7.35"), null);

        // Assert
        assertThat(entrada.produtoId()).isEqualTo(15L);
        assertThat(entrada.estoqueAnterior()).isEqualByComparingTo("18.500");
        assertThat(entrada.estoqueAtual()).isEqualByComparingTo("31.000");
        assertThat(entrada.precoCustoAnterior()).isEqualByComparingTo("6.90");
        assertThat(entrada.precoCustoAtual()).isEqualByComparingTo("7.35");
    }

    @Test
    void deveEnviarAoPortValoresNormalizadosDataAtualEMaximoDeEstoque() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto("1.000", "1.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        useCase.executar(15L, new BigDecimal("12.5"), new BigDecimal("7.3"), null);

        // Assert
        ArgumentCaptor<NovaEntradaEstoque> captor = ArgumentCaptor.forClass(NovaEntradaEstoque.class);
        verify(registrarEntradaOutputPort).registrarEntrada(eq(15L), captor.capture());
        NovaEntradaEstoque enviado = captor.getValue();
        assertThat(enviado.quantidade()).isEqualTo(new BigDecimal("12.500"));
        assertThat(enviado.precoCusto()).isEqualTo(new BigDecimal("7.30"));
        assertThat(enviado.dataAtualizacao()).isEqualTo(HOJE);
        assertThat(enviado.limiteEstoque()).isEqualByComparingTo("9999999.999");
    }

    @Test
    void naoDeveEnviarDataDeVencimentoQuandoUsuarioNaoInformou() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto("1.000", "1.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        useCase.executar(15L, BigDecimal.ONE, BigDecimal.ONE, null);

        // Assert
        ArgumentCaptor<NovaEntradaEstoque> captor = ArgumentCaptor.forClass(NovaEntradaEstoque.class);
        verify(registrarEntradaOutputPort).registrarEntrada(eq(15L), captor.capture());
        assertThat(captor.getValue().dataVencimento()).isNull();
    }

    @Test
    void deveEnviarDataDeVencimentoQuandoInformada() {
        // Arrange
        LocalDate vencimento = LocalDate.of(2027, 3, 15);
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto("1.000", "1.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        useCase.executar(15L, BigDecimal.ONE, BigDecimal.ONE, vencimento);

        // Assert
        ArgumentCaptor<NovaEntradaEstoque> captor = ArgumentCaptor.forClass(NovaEntradaEstoque.class);
        verify(registrarEntradaOutputPort).registrarEntrada(eq(15L), captor.capture());
        assertThat(captor.getValue().dataVencimento()).isEqualTo(vencimento);
    }

    @Test
    void deveTratarEstoqueAnteriorNuloComoZeroEManterCustoAnteriorNulo() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(
                Optional.of(produto(null, null)), Optional.of(produto("5.000", "7.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        EntradaEstoque entrada = useCase.executar(15L, new BigDecimal("5"), new BigDecimal("7"), null);

        // Assert
        assertThat(entrada.estoqueAnterior()).isEqualByComparingTo("0");
        assertThat(entrada.estoqueAtual()).isEqualByComparingTo("5.000");
        assertThat(entrada.precoCustoAnterior()).isNull();
        assertThat(entrada.precoCustoAtual()).isEqualByComparingTo("7.00");
    }

    @Test
    void deveRetornarEstoqueRealLidoAposAtualizacao() {
        // Arrange: uma venda concorrente reduziu o estoque entre a leitura inicial e a final
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(
                Optional.of(produto("10.000", "1.00")), Optional.of(produto("18.000", "2.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(true);

        // Act
        EntradaEstoque entrada = useCase.executar(15L, new BigDecimal("10"), new BigDecimal("2"), null);

        // Assert
        assertThat(entrada.estoqueAtual()).isEqualByComparingTo("18.000");
    }

    @Test
    void deveRejeitarIdInvalidoSemConsultarNemGravar() {
        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(0L, BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(null, BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(buscarProdutoOutputPort, registrarEntradaOutputPort);
    }

    @Test
    void naoDeveGravarQuandoQuantidadeInvalida() {
        // Act / Assert
        for (BigDecimal quantidade : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"),
                new BigDecimal("1.0001"), new BigDecimal("10000000")}) {
            assertThatThrownBy(() -> useCase.executar(15L, quantidade, BigDecimal.ONE, null))
                    .isInstanceOf(QuantidadeInvalidaException.class);
        }
        verifyNoInteractions(registrarEntradaOutputPort);
    }

    @Test
    void naoDeveGravarQuandoCustoInvalido() {
        // Act / Assert
        for (BigDecimal custo : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"),
                new BigDecimal("1.001"), new BigDecimal("100000000")}) {
            assertThatThrownBy(() -> useCase.executar(15L, BigDecimal.ONE, custo, null))
                    .isInstanceOf(PrecoInvalidoException.class);
        }
        verifyNoInteractions(registrarEntradaOutputPort);
    }

    @Test
    void deveLancarNaoEncontradoQuandoProdutoNaoExiste() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(99L, BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        verify(registrarEntradaOutputPort, never()).registrarEntrada(any(), any());
    }

    @Test
    void deveLancarNaoEncontradoQuandoUpdateNaoAlteraLinhaEProdutoSumiu() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(
                Optional.of(produto("1.000", "1.00")), Optional.empty());
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(15L, BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deveLancarLimiteExcedidoQuandoUpdateNaoAlteraLinhaEProdutoExiste() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto("9999999.000", "1.00")));
        when(registrarEntradaOutputPort.registrarEntrada(eq(15L), any())).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(15L, new BigDecimal("5"), BigDecimal.ONE, null))
                .isInstanceOf(EstoqueLimiteExcedidoException.class);
    }

    private static Produto produto(String estoque, String custo) {
        return new Produto(15L, "123", "Produto", "UN", new BigDecimal("9.50"),
                custo == null ? null : new BigDecimal(custo), "12345678",
                estoque == null ? null : new BigDecimal(estoque));
    }
}
