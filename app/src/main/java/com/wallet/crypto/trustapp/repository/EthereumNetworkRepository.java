package com.wallet.crypto.trustapp.repository;

import android.text.TextUtils;

import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Ticker;
import com.wallet.crypto.trustapp.service.TickerService;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Single;

import static com.wallet.crypto.trustapp.C.ESN_NETWORK_NAME;
import static com.wallet.crypto.trustapp.C.ETHEREUM_NETWORK_NAME;
import static com.wallet.crypto.trustapp.C.ESN_SYMBOL;
import static com.wallet.crypto.trustapp.C.ETH_SYMBOL;

public class EthereumNetworkRepository implements EthereumNetworkRepositoryType {

	private final NetworkInfo[] NETWORKS = new NetworkInfo[] {
            new NetworkInfo(ESN_NETWORK_NAME, ESN_SYMBOL,
                    "https://mainnet.infura.io/llyrtzQ3YhkdESt2Fzrk",
                    "https://api.trustwalletapp.com/",
                    "https://etherscan.io/",31102, true),
            new NetworkInfo(ETHEREUM_NETWORK_NAME, ETH_SYMBOL,
                    "https://mainnet.infura.io/llyrtzQ3YhkdESt2Fzrk",
                    "https://api.trustwalletapp.com/",
                    "https://etherscan.io/",1, true),
	};

	private final PreferenceRepositoryType preferences;
    private final TickerService tickerService;
    private NetworkInfo defaultNetwork;
    private final Set<OnNetworkChangeListener> onNetworkChangedListeners = new HashSet<>();

    public EthereumNetworkRepository(PreferenceRepositoryType preferenceRepository, TickerService tickerService) {
		this.preferences = preferenceRepository;
		this.tickerService = tickerService;
		defaultNetwork = getByName(preferences.getDefaultNetwork());
		if (defaultNetwork == null) {
			defaultNetwork = NETWORKS[0];
		}
	}

	private NetworkInfo getByName(String name) {
		if (!TextUtils.isEmpty(name)) {
			for (NetworkInfo NETWORK : NETWORKS) {
				if (name.equals(NETWORK.name)) {
					return NETWORK;
				}
			}
		}
		return null;
	}

	@Override
	public NetworkInfo getDefaultNetwork() {
		return defaultNetwork;
	}

	@Override
	public void setDefaultNetworkInfo(NetworkInfo networkInfo) {
		defaultNetwork = networkInfo;
		preferences.setDefaultNetwork(defaultNetwork.name);

		for (OnNetworkChangeListener listener : onNetworkChangedListeners) {
		    listener.onNetworkChanged(networkInfo);
        }
	}

	@Override
	public NetworkInfo[] getAvailableNetworkList() {
		return NETWORKS;
	}

	@Override
	public void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged) {
        onNetworkChangedListeners.add(onNetworkChanged);
	}

    @Override
    public Single<Ticker> getTicker() {
        return Single.fromObservable(tickerService
                .fetchTickerPrice(getDefaultNetwork().symbol));
    }
}
